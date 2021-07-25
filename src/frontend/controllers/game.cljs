(ns frontend.controllers.game
  (:require
   [frontend.websockets.core :as ws]
   [re-frame.core :as rf]
   [taoensso.sente :as sente]))

(rf/reg-fx
 :game-start-fx
 (fn [rid]
   ((:send-fn ws/client-chsk)
    [:game/start {:rid rid}]
    1000
    (fn [reply]
      (if (sente/cb-success? reply)
        (js/console.log "Success, game started. Room is now closed.")
        (js/alert (str "Oops, you have a problem starting the game for room #" rid "...")))))))

(rf/reg-event-fx
 ::start
 (fn [_ [_ rid]]
   {:game-start-fx rid}))

(rf/reg-event-db
 ::update
 (fn [db [_ game]]
   (assoc db :game game)))

(rf/reg-sub
 ::status
 (fn [db _]
   (get-in db [:game :game/status])))
