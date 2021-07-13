(ns backend.router
  (:require
   [backend.routes.ws :as ws]
   [clj-uuid :as uuid]
   [clojure.set :as set]
   [clojure.tools.logging :as log]
   [mount.core :refer [defstate]]
   [taoensso.sente :as sente]))

(defn- broadcast!
  ([event]
   (broadcast! event (:any @(:connected-uids ws/server-chsk))))
  ([event uids]
   (doseq [uid uids]
     ((:send-fn ws/server-chsk) uid event))))

(defmulti event-msg-handler :id)

(defmethod event-msg-handler :default
  [{:keys [_event id _?data _ring-req _?reply-fn _send-fn] :as _payload}]
  (log/infof "Unhandled event: %s" id)
  #_(log/debugf "With payload\n%s" (utils/pp-str payload)))

(defmethod event-msg-handler :player-name/fetch
  [_] ;; For illustrative purposes only
  (broadcast! [:player-name/publish {:player-map @ws/*player-map}]))

;; TODO better persistence
(defonce registered-rooms (atom {}))

(defmethod event-msg-handler :room/create
  [{?data :?data}]
  (let [rid (uuid/v4)
        uid (:user-id ?data)]
    (swap! registered-rooms assoc rid {:host uid :players #{uid}})
    (broadcast! [:room/enter {:rid rid}] #{uid})
    (broadcast! [:room/register {:rid rid :user-id uid}])))

(defmethod event-msg-handler :room/join
  [{f :?reply-fn data :?data}]
  (let [{rid :rid user-id :user-id} data
        player-count (count (get @registered-rooms rid))
        other-rooms (dissoc @registered-rooms rid)
        occupied-players (reduce (fn [acc [_ {players :players}]]
                                   (set/union acc players))
                                 #{}
                                 other-rooms)]
    (when f
      (if (and (< 0 player-count 4) ;; room has available spots. 
               (not (contains? occupied-players user-id))) ;; client isn't already part of another existing room.
        (let [updated-rooms (swap! registered-rooms update-in [rid :players] conj user-id) ;; idempotent operation.
              connected-players (get-in updated-rooms [rid :players])]
          (broadcast! [:room/update {:rid rid :connected-players connected-players}])
          (f :chsk/success))
        (f :chsk/error)))))

(defmethod event-msg-handler :room/leave
  [{f :?reply-fn data :?data}]
  (let [{host? :host?
         user-id :user-id
         rid :rid} data
        {players :players :as room} (get @registered-rooms rid)]
    (when f ;; TODO this section illustrates that the dual copies of room state in frontend and backend is getting hard to synchronize.
      (if (and room (contains? players user-id)) ;; The room exists and the user is in it. 
        (let [leaving-players (if host?
                                players  ;; kicks everyone out of the room if the host leaves.
                                #{user-id}) ;; if not the host, then just the requesting user leaves.
              remaining-players (set/difference players leaving-players)]
          (if (empty? remaining-players)
            (swap! registered-rooms dissoc rid)  ;; drop the room from the registry if there are no more players.
            (swap! registered-rooms update-in [rid :players] disj user-id))  ;; drop the player from room
          (broadcast! [:room/exit {}] leaving-players)
          (broadcast! [:room/update {:rid rid :connected-players remaining-players}])
          (f :chsk/success))
        (f :chsk/error))))) 

(defmethod event-msg-handler :room/get-ids
  [{:keys [?reply-fn]}]
  (when ?reply-fn
    (?reply-fn {:rooms @registered-rooms})))

;; TODO Other handlers for default sente events...

(defmethod event-msg-handler :chsk/uidport-open
  [{?data :?data}]
  (log/info "Server " (pr-str ?data)))

(defstate router
  :start (sente/start-server-chsk-router!
          (:ch-recv ws/server-chsk)
          event-msg-handler))

(comment
  (reset! registered-rooms {}))
