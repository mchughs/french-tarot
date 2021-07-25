(ns frontend.controllers.players
  (:require
   [re-frame.core :as rf]))

;; Get the name of a single user from their id.
(rf/reg-sub
 ::name ;; Likely to change in implementation.
 (fn [db [_ uid]]
   (get-in db [:users uid :user/name])))

(rf/reg-event-db
 ::update
 (fn [db [_ player]]
   (assoc db :player player)))

(rf/reg-sub
 ::order
 (fn [db _]
   (sort-by :position (:players db))))

(rf/reg-sub
 ::player
 (fn [db _]
   (:player db)))

(rf/reg-sub
 ::hand
 :<- [::player]
 (fn [player _]
   (get player :player/hand)))

(rf/reg-sub
 ::score
 :<- [::player]
 (fn [player _]
   (get player :player/score)))
