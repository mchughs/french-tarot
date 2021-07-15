(ns backend.router
  (:require
   [backend.models.room :as room]
   [backend.models.round :as round]
   [backend.routes.ws :as ws]
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
  #_(log/infof "With payload\n%s" (utils/pp-str payload)))

(defmethod event-msg-handler :player-name/fetch
  [_] ;; For illustrative purposes only
  (broadcast! [:player-name/publish {:player-map @ws/*player-map}]))

;; TODO better persistence
(defonce registered-rooms (atom {}))

(defmethod event-msg-handler :room/create
  [{uid :uid}]
  (let [{rid :rid :as new-room} (room/create uid)]
    (swap! registered-rooms assoc rid new-room)
    (broadcast! [:frontend.controllers.room/enter {:rid rid}] #{uid})
    (broadcast! [:frontend.controllers.room/register {:rid rid :user-id uid}])))

(defmethod event-msg-handler :room/join
  [{f :?reply-fn ?data :?data uid :uid}]
  (let [{rid :rid} ?data]
    (when f
      (if (room/can-join? @registered-rooms rid uid)
        (let [new-room (room/add-player (get @registered-rooms rid)
                                        uid)]
          (swap! registered-rooms assoc rid new-room) ;; idempotent operation.
          (broadcast! [:frontend.controllers.room/update {:rid rid :connected-players (:players new-room)}])
          (f :chsk/success))
        (f :chsk/error)))))

(defmethod event-msg-handler :room/leave
  [{f :?reply-fn ?data :?data uid :uid}]
  (let [{rid :rid} ?data
        room (get @registered-rooms rid)]
    (when f
      (if (room/can-leave? @registered-rooms rid uid)
        (let [new-room (room/remove-player room uid)] ;; removes all players if the host leaves
          (if new-room
            (swap! registered-rooms assoc rid new-room)
            (swap! registered-rooms dissoc rid)) ;; drop the room from the registry if there are no more players. 
          (let [{:keys [players host]} room
                leaving-players (if (= host uid) players #{uid})]
            (broadcast! [:frontend.controllers.room/exit {}] leaving-players))          
          (broadcast! [:frontend.controllers.room/update {:rid rid :connected-players (:players new-room)}])          
          (f :chsk/success))
        (f :chsk/error)))))

(defmethod event-msg-handler :game/start
  [{f :?reply-fn data :?data uid :uid}]
  (let [{rid :rid} data
        {players :players
         host :host
         closed? :closed?
         :as room} (get @registered-rooms rid)
        host? (= uid host)]
    (when f 
      (if (and room host? (= 4 (count players)) (not closed?)) ;; The room exists, is full, the game request was initiated by the host, and the game isn't already started. 
        (do
          (swap! registered-rooms update rid assoc :closed? true)
          (broadcast! [:frontend.controllers.room/close {:rid rid}])
          (f :chsk/success))
        (f :chsk/error)))))

(defn- broadcast-round! [round]
  (doseq [player (:players round)
          :let [event [:round/deal player]
                uid (:id player)]]
    (broadcast! event [uid])))

(defmethod event-msg-handler :round/start
  [{f :?reply-fn data :?data uid :uid}]
  (let [{rid :rid} data
        {:keys [players host closed? round-history] :as room} (get @registered-rooms rid)
        host? (= uid host)]
    (when f      
      (if (and room host? (= 4 (count players)) closed?)
        (let [new-round-history (if (empty? round-history)
                                  (round/init-history players)
                                  (round/add-next round-history))]
          (swap! registered-rooms update rid assoc :round-history new-round-history)
          (broadcast-round! (last new-round-history))
          (f :chsk/success))
        (f :chsk/error)))))

(defmethod event-msg-handler :round/end
  [{f :?reply-fn}] ;; TODO
  (when f
    (f :chsk/success)))

(defmethod event-msg-handler :room/get-ids
  [{f :?reply-fn}]
  (when f
    (f {:rooms @registered-rooms})))

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
