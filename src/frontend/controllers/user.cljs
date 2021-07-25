(ns frontend.controllers.user
  "For all functions regarding the operating user. Not other users."
  (:require
   [re-frame.core :as rf]))

(rf/reg-sub
 ::id
 (fn [db _]
   (get db :user/id)))

(rf/reg-sub
 ::user
 (fn [db _]
   (let [uid (get db :user/id)]
     (get-in db [:users uid]))))

(rf/reg-sub
 ::room
 :<- [::user]
 (fn [user _]
   (get user :user/room)))

(rf/reg-sub
 ::name
 :<- [::user]
 (fn [user _]
   (get user :user/name)))

(rf/reg-event-db
 ::update
 (fn [db [_ {uid :uid user :user}]]
   (if user
     (update db :users assoc uid user)
     (update db :users dissoc uid))))

(rf/reg-event-db
 ::register-all
 (fn [db [_ users]]
   (assoc db :users users)))
