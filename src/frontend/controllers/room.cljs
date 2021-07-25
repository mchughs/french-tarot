(ns frontend.controllers.room
  (:require
   [re-frame.core :as rf]
   [frontend.router.events :as router.events]
   [frontend.websockets.core :as ws]
   [taoensso.sente :as sente]))

(rf/reg-sub
 ::rooms
 (fn [db _]
   (get db :rooms)))

(rf/reg-sub
 ::room
 (fn [db [_ rid]]
   (get-in db [:rooms rid])))

(rf/reg-event-db
 ::register-all
 (fn [db [_ rooms]]
   (assoc db :rooms rooms)))

(rf/reg-event-db
 ::update
 (fn [db [_ {rid :rid room :room}]]
   (if room 
     (update db :rooms assoc rid room)
     (update db :rooms dissoc rid)))) ;; If there are no more players in the room, delete it.

(rf/reg-event-fx
 ::enter
 (fn [_ [_ {rid :rid}]]
   {::router.events/to-roompage rid}))

(rf/reg-fx
 :join-fx
 (fn [[rid user-id]]
   ((:send-fn ws/client-chsk)
    [:room/join {:rid rid :user-id user-id}]
    1000
    (fn [reply]
      (if (sente/cb-success? reply)
        (rf/dispatch [::enter {:rid rid}])
        (js/alert (str "Oops, you have a problem joining room #" rid "...")))))))

(rf/reg-event-fx
 ::join
 (fn [{db :db} [_ rid]]
   {:join-fx [rid (:user/id db)]}))

(rf/reg-event-fx
 ::exit
 (fn [_ _]
   {::router.events/to-homepage _}))

(rf/reg-fx
 :leave-fx
 (fn [{:keys [_user-id _host? rid] :as payload}]
   ((:send-fn ws/client-chsk)
    [:room/leave payload]
    1000
    (fn [reply]
      (if (sente/cb-success? reply)
        (rf/dispatch [::exit])
        (js/alert (str "Oops, you have a problem leaving room #" rid "...")))))))

(rf/reg-event-fx
 ::leave
 (fn [_ [_ payload]]
   {:leave-fx payload}))
