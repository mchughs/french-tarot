(ns frontend.controllers.players
  (:require
   [re-frame.core :as rf]))

;; Get the name of a single user from their id.
(rf/reg-sub
 ::name ;; Likely to change in implementation.
 (fn [db [_ uid]]
   (get (:player-map db) uid)))

(rf/reg-event-db
 ::update
 (fn [db [_ players]]
   (assoc db :players players)))

(rf/reg-sub
 ::order
 (fn [db _]
   (sort-by :position (:players db))))
