(ns frontend.controllers.card
  (:require
   [re-frame.core :as rf]))

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
