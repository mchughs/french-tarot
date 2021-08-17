(ns frontend.controllers.card
  (:require
   [frontend.websockets.core :as ws]
   [re-frame.core :as rf]
   [taoensso.sente :as sente]))

(defn can-set-aside? [{:keys [type name] :as _card}] ;; TODO technically you can set aside kings or trumps if you have no choice. Super unlikely to happen though.
  (or (= :pip type)
      (and (= :face type)
           (not= :king name))))

(rf/reg-event-db
 ::set-aside
 (fn [{init-taker-pile :init-taker-pile :as db} [_ card]]
   (if (can-set-aside? card)
     (if (nil? init-taker-pile)
       (assoc db :init-taker-pile #{card})
       (update db :init-taker-pile conj card))
     db))) ;; TODO add an error to display. Need generic error dialog box.

(rf/reg-event-db
 ::recover
 (fn [db [_ card]]
   (update db :init-taker-pile disj card)))

(rf/reg-sub
 ::init-taker-pile
 (fn [db _]
   (:init-taker-pile db)))

(rf/reg-fx
 :card/play
 (fn [payload]
   ((:send-fn ws/client-chsk)
    [:card/play payload]
    4000
    (fn [reply]
      (if (sente/cb-success? reply)
        (js/console.log "Success, card/play.")
        (js/alert (str "Oops, you have a problem card/play for room #" reply "...")))))))

(rf/reg-event-fx
 ::play
 (fn [{db :db} [_ card]]
   {:card/play {:log-id (get-in db [:curr/log :log/id])
                :card card}}))
