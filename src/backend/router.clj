(ns backend.router
  (:require
   [backend.models.game :as game]
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
          (broadcast! [:frontend.controllers.room/update {:rid rid :room new-room}])
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
          (broadcast! [:frontend.controllers.room/update {:rid rid :room new-room}])          
          (f :chsk/success))
        (f :chsk/error)))))

(defmethod event-msg-handler :game/start
  [{f :?reply-fn data :?data uid :uid}]
  (let [{rid :rid} data
        room (get @registered-rooms rid)]
    (when f
     (if (game/can-start? room uid)
        (let [new-room (assoc room :game-status :in-progress)]
          (swap! registered-rooms assoc rid new-room)
          (broadcast! [:frontend.controllers.room/update {:rid rid :room new-room}])
          (f :chsk/success))
        (f :chsk/error)))))

(defn- broadcast-round! [round]
  (doseq [player (:players round)
          :let [round-log (:round-log round)
                event [:round/deal {:player-data player :round-log round-log}]
                uid (:id player)]]
    (broadcast! event [uid])))

(defmethod event-msg-handler :round/start
  [{f :?reply-fn ?data :?data uid :uid}]
  (let [{rid :rid} ?data
        {:keys [players round-history] :as room} (get @registered-rooms rid)]
    (when f
      (if (round/can-start? room uid)
        (let [new-round-history (if (empty? round-history)
                                  (round/init-history players)
                                  (round/add-next round-history))
              new-room (assoc room :game-status :playing-round)]
          ;; round-history data should not be exposed to players as it contains information that should be hidden
          (swap! registered-rooms assoc rid (assoc new-room :round-history new-round-history))
          (broadcast-round! (last new-round-history))
          (broadcast! [:frontend.controllers.room/update {:rid rid :room new-room}])
          (f :chsk/success))
        (f :chsk/error)))))

(defmethod event-msg-handler :round/end
  [{f :?reply-fn ?data :?data}]
  (let [{rid :rid} ?data
        {:keys [game-status] :as room} (get @registered-rooms rid)]
    (when f
      (if (= :playing-round game-status)
        (let [new-room (assoc room :game-status :in-progress)]
          (swap! registered-rooms assoc rid new-room)
          (broadcast! [:frontend.controllers.room/update {:rid rid :room new-room}])
          (f :chsk/success))
        (f :chsk/error)))))

(defmethod event-msg-handler :round/place-bid
  [{f :?reply-fn ?data :?data uid :uid}]
  (let [{rid :rid bid :bid} ?data
        {:keys [game-status round-history] :as room} (get @registered-rooms rid)]
    (when f
      (if (= :playing-round game-status)
        (let [new-round-history (round/place-bid round-history bid uid)]
          (swap! registered-rooms assoc rid (assoc room :round-history new-round-history))
          (broadcast-round! (last new-round-history))
          (f :chsk/success))
        (f :chsk/error)))))

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
