(ns backend.router
  (:require
   [backend.routes.ws :as ws]
   [clj-uuid :as uuid]
   [clojure.set :as set]
   [clojure.tools.logging :as log]
   [mount.core :refer [defstate]]
   [taoensso.sente :as sente]))

(defn- broadcast! [event]
  (doseq [uid (:any @(:connected-uids ws/server-chsk))]
    ((:send-fn ws/server-chsk) uid event)))

(defmulti event-msg-handler :id)

(defmethod event-msg-handler :default
  [{:keys [_event id _?data _ring-req _?reply-fn _send-fn] :as _payload}]
  (log/infof "Unhandled event: %s" id)
  #_(log/debugf "With payload\n%s" (utils/pp-str payload)))

;; TODO better persistence
(defonce registered-rooms (atom {}))

(defmethod event-msg-handler :room/create
  [{?data :?data}]
  (let [rid (uuid/v4)
        uid (:user-id ?data)]
    (swap! registered-rooms assoc rid {:host uid :players #{uid}})
    (broadcast! [:room/register {:rid rid :user-id uid}])))

(defmethod event-msg-handler :room/join
  [{f :?reply-fn data :?data}]
  (let [{rid :rid user-id :user-id} data
        player-count (count (get @registered-rooms rid))
        occupied-players (reduce (fn [acc [_ {players :players}]]
                                   (set/union acc players))
                                 #{}
                                 @registered-rooms)]
    (when f
      (if (and (< 0 player-count 4) ;; room has an available spot.
               (not (contains? occupied-players user-id))) ;; client isn't already part of an existing room.
        (let [updated-rooms (swap! registered-rooms update-in [rid :players] conj user-id)
              connected-players (get-in updated-rooms [rid :players])]
          (broadcast! [:room/update {:rid rid
                                     :connected-players connected-players}])
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
