(ns backend.router
  (:require
   [backend.db :as db]
   [backend.models.cards :as cards]
   [backend.models.deck :as deck]
   [backend.models.game :as game]
   [backend.models.logs :as logs]
   [backend.models.players :as players]
   [backend.models.room :as room]
   [backend.models.round :as round]
   [backend.models.user :as user]
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

(defmethod event-msg-handler :room/create
  [{uid :uid}]
  (let [{rid :room/id :as new-room} (room/create uid)]
    (db/insert! rid new-room)
    (user/host-room! uid rid)
    (broadcast! [:frontend.controllers.room/enter
                 {:rid rid}]
                #{uid})
    (broadcast! [:frontend.controllers.room/update
                 {:rid rid
                  :room new-room}])))

(defn- broadcast-room! [rid]
  (broadcast! [:frontend.controllers.room/update
               {:rid rid
                :room (room/get-room rid)}]))

(defmethod event-msg-handler :room/join
  [{f :?reply-fn ?data :?data uid :uid}]
  (let [{rid :rid} ?data]
    (when f
      (if (room/can-join? rid uid)
        (do
          (room/add-player! rid uid) ;; idempotent operation.
          (user/join-room! uid rid)
          (broadcast-room! rid)
          (f :chsk/success))
        (f :chsk/error)))))

(defmethod event-msg-handler :room/leave
  [{f :?reply-fn ?data :?data uid :uid}]
  (let [{rid :rid} ?data]
    (when f
      (if (room/can-leave? rid uid)
        (let [leaving-players (room/remove-player! rid uid)]          
          (doseq [user-id leaving-players]
            (user/leave-room! user-id rid))
          (broadcast! [:frontend.controllers.room/exit
                       {}]
                      leaving-players)
          (broadcast-room! rid)
          (f :chsk/success))
        (f :chsk/error)))))

(defmethod event-msg-handler :game/start
  [{f :?reply-fn data :?data uid :uid}]
  (let [{rid :rid} data]
    (when f
     (if (game/can-start? rid uid)
        (let [gid (game/create! rid)
              uids (players/create! rid gid)]
          (game/start! rid gid)
          (broadcast-room! rid)
          (broadcast! [:frontend.controllers.game/update (game/get-game gid)]
                      uids)
          (broadcast! [:frontend.controllers.players/fetch (players/get-public-player-data gid)]
                      uids)
          (f :chsk/success))
        (f :chsk/error)))))

(defmethod event-msg-handler :round/start
  [{f :?reply-fn ?data :?data uid :uid}]
  (let [{rid :rid gid :gid} ?data]
    (when f
      (if (round/can-start? rid uid)
        (let [log-id (logs/init-log!)
              round-id (round/start! gid log-id)
              uids (players/get-players gid)]
          (deck/deal! gid round-id)
          (game/begin-round! gid)
          (broadcast! [:frontend.controllers.round/update (round/get-round round-id)]
                      uids)          
          (broadcast! [:frontend.controllers.game/update (game/get-game gid)]
                      uids)          
          (broadcast! [:frontend.controllers.log/update (logs/get-log log-id)]
                      uids)
          (doseq [uid uids]
            (broadcast! [:frontend.controllers.players/update (players/get-player uid)]
                        [uid]))
          (f :chsk/success))
        (f :chsk/error)))))

(defmethod event-msg-handler :log/place-bid
  [{f :?reply-fn ?data :?data uid :uid}]
  (let [{:keys [log-id bid gid]} ?data]
    (when f
      (if-let [log (logs/get-log log-id)]
        (let [uids (players/log->players log-id)
              new-log (logs/place-bid! log bid uid uids)
              new-log-id (logs/add-log! new-log log)
              round-id (round/append-log! (:log/id log) new-log-id)]              
          (broadcast! [:frontend.controllers.round/update (round/get-round round-id)]
                      uids)
          (broadcast! [:frontend.controllers.log/update (logs/get-log new-log-id)]
                      uids)
          (when (= :end (:log/phase new-log))
            (game/end-round! gid)
            (broadcast! [:frontend.controllers.game/update (game/get-game gid)]
                        uids))
          (f :chsk/success))
        (f :chsk/error)))))

(defmethod event-msg-handler :log/make-announcement
  [{f :?reply-fn ?data :?data uid :uid}]
  (let [{log-id :log-id announcement :announcement} ?data
        old-log (logs/get-log log-id)]
    (when f
      (if (= :announcements (:log/phase old-log))
        (let [uids (players/log->players log-id)
              round (logs/log-id->round log-id)
              new-log (-> old-log
                          (logs/make-announcement announcement uid)
                          (logs/make-dog (:round/dog round)))
              new-log-id (logs/add-log! new-log old-log)
              round-id (round/append-log! (:log/id old-log) new-log-id)]
          (when-let [dog (:log/dog new-log)]
            (players/add-dog-to-hand! (players/uid->pid uid) dog)
            (broadcast! [:frontend.controllers.players/update (players/get-player uid)]
                        [uid]))
          (broadcast! [:frontend.controllers.round/update (round/get-round round-id)]
                      uids)
          (broadcast! [:frontend.controllers.log/update (logs/get-log new-log-id)]
                      uids)
          ;; TODO piles should not be broadcast out
          (f :chsk/success))
        (f :chsk/error)))))

(defmethod event-msg-handler :log/submit-dog
  [{f :?reply-fn ?data :?data uid :uid}]
  (let [{log-id :log-id init-taker-pile :init-taker-pile} ?data
        old-log (logs/get-log log-id)]
    (when f
      (if (and (= :dog-construction (:log/phase old-log))
               (= uid (get-in old-log [:log/taker :uid]))
               (= 6 (count init-taker-pile)))
          (let [uids (players/log->players log-id)
                new-log (logs/init-dog old-log init-taker-pile)
                new-log-id (logs/add-log! new-log old-log)
                round-id (round/append-log! (:log/id old-log) new-log-id)]
            (players/remove-dog-from-hand! (players/uid->pid uid) init-taker-pile)            
            (broadcast! [:frontend.controllers.players/update (players/get-player uid)]
                        [uid])
            (broadcast! [:frontend.controllers.round/update (round/get-round round-id)]
                        uids)
            (broadcast! [:frontend.controllers.log/update (logs/get-log new-log-id)]
                        uids)
            (f :chsk/success))
          (f :chsk/error)))))

(defmethod event-msg-handler :card/play
  [{f :?reply-fn ?data :?data uid :uid}]
  (let [{log-id :log-id card :card} ?data
        old-log (logs/get-log log-id)]
    (when f      
      (if (and (= :main (:log/phase old-log))
               (logs/user-turn? old-log uid)
               (logs/allowed-card? old-log card uid))        
        (let [uids (players/log->players log-id)
              new-log (logs/play-card old-log card uid)
              new-log-id (logs/add-log! new-log old-log)
              round-id (round/append-log! (:log/id old-log) new-log-id)]
          (cards/remove-card-from-hand! (players/uid->pid uid) card)
          (broadcast! [:frontend.controllers.players/update (players/get-player uid)]
                      [uid])
          (broadcast! [:frontend.controllers.round/update (round/get-round round-id)]
                      uids)
          (broadcast! [:frontend.controllers.log/update (logs/get-log new-log-id)]
                      uids)
          (f :chsk/success))
        (f :chsk/error)))))

(defmethod event-msg-handler :round/score
  [{f :?reply-fn ?data :?data}]
  (let [{log-id :log-id gid :gid} ?data
        old-log (logs/get-log log-id)]
    (when f
      (if (= :scoring (:log/phase old-log))
        (let [uids (players/log->players log-id)
              new-log (round/score old-log)
              new-log-id (logs/add-log! new-log old-log)
              round-id (round/append-log! (:log/id old-log) new-log-id)]          
          (round/next-dealer! round-id)
          (players/append-scores! new-log uids)
          (game/end-round! gid)
          (doseq [uid uids]
            (broadcast! [:frontend.controllers.players/update (players/get-player uid)]
                        [uid]))
          (broadcast! [:frontend.controllers.round/update (round/get-round round-id)]
                      uids)
          (broadcast! [:frontend.controllers.log/update (logs/get-log new-log-id)]
                      uids)
          (broadcast! [:frontend.controllers.game/update (game/get-game gid)]
                      uids)          
          (f :chsk/success))
        (f :chsk/error)))))

(defmethod event-msg-handler :rooms/get
  [{f :?reply-fn}]
  (when f
    (f (room/get-rooms))))

(defmethod event-msg-handler :user/get
  [{f :?reply-fn uid :uid}]
  (when f
    (f (user/get-user uid))))
 
;; TODO Other handlers for default sente events...

(defmethod event-msg-handler :chsk/uidport-open
  [{uid :uid}]
  (let [user (user/create uid)]
    (db/insert! uid user)
    (broadcast! [:frontend.controllers.user/update (user/get-user uid)]
                [uid])))

(defmethod event-msg-handler :user/submit
  [{f :?reply-fn ?data :?data uid :uid}]
  (when f
    (let [{username :user/name} ?data]
      (when-not (user/has-name? uid username)
        (user/give-name! uid username))      
      (f username))))

(defstate router
  :start (sente/start-server-chsk-router!
          (:ch-recv ws/server-chsk)
          event-msg-handler))
