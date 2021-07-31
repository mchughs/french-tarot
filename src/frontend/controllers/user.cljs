(ns frontend.controllers.user
  "For all functions regarding the operating user. Not other users."
  (:require
   [frontend.cookies :as cookies]
   [frontend.websockets.core :as ws]
   [re-frame.core :as rf]
   [taoensso.sente :as sente]))

(rf/reg-sub
 ::id
 (fn [db _]
   (get db :user/id)))

(rf/reg-sub
 ::room
 (fn [db _]
   (get db :user/room)))

(rf/reg-sub
 ::name
 (fn [db _]
   (get db :user/name)))

(rf/reg-sub
 ::missing-name?
 :<- [::name]
 (fn [name _]
   (not name)))

(rf/reg-event-fx
 ::set-name
 (fn [{db :db} [_ username]]
   {::cookies/set-username username
    :db (assoc db :user/name username)}))

(rf/reg-fx
 :user/submit
 (fn [username]
   ((:send-fn ws/client-chsk)
    [:user/submit {:user/name username}]
    1000
    (fn [reply]
      (if (sente/cb-success? reply)
        (rf/dispatch [::set-name reply])
        (js/alert "Sorry, a user already has taken that name. Please choose another name."))))))

(rf/reg-event-fx
 ::submit-name
 (fn [_ [_ username]]
  {:user/submit username}))

(rf/reg-event-db
 ::update
 (fn [db [_ user]]
   (merge db user)))
