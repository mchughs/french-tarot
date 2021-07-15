(ns frontend.controllers.game
  (:require
   [frontend.websockets.core :as ws]
   [re-frame.core :as rf]
   [taoensso.sente :as sente]))

;; TODO needs to be changed
(rf/reg-event-db
 :startable-fx
 (fn [db _]
   (assoc db :round/startable true)))

(rf/reg-fx
 :game-start-fx
 (fn [rid]
   ((:send-fn ws/client-chsk)
    [:game/start {:rid rid}]
    1000
    (fn [reply]
      (if (sente/cb-success? reply)
        (rf/dispatch [:startable-fx])
        (js/alert (str "Oops, you have a problem starting the game for room #" rid "...")))))))

(rf/reg-event-fx
 ::start
 (fn [_ [_ rid]]
   {:game-start-fx rid}))
