(ns frontend.events
  (:require [re-frame.core :as rf]
            [reitit.frontend.easy :as rfe]
            [frontend.cookies :as cookies]))

(rf/reg-event-db
 ::load-page
 ;; TODO initialize rf-db data for a client.
 (fn [db _]
   db))

(rf/reg-event-fx
 ::set-uid
 [(rf/inject-cofx ::cookies/uid)]
 (fn [{db :db stored-uid :cookie/uid} [_ uid]]
   (if false #_stored-uid ;; TODO just makes it easier to develop 
     {:db (assoc db :user/id stored-uid)}
     {:db (assoc db :user/id uid)
      ::cookies/set-uid uid})))

(rf/reg-event-db
 ::open
 (fn [db _]
   (assoc db :chsk/open true)))

(rf/reg-event-db
 ::update-page-match
 (fn [db [_ match]]
   (assoc db :page/match match)))

(rf/reg-fx
 ::send-to-room
 (fn [rid]
   (rfe/push-state :router/room-lobby {:rid rid})))

(rf/reg-event-db
 :room/register
 (fn [db [_ {rid :rid user-id :user-id}]]
   (assoc-in db [:rooms rid] {:host user-id :players #{user-id}})))

(rf/reg-event-db
 :room/update
 (fn [db [_ {rid :rid connected-players :connected-players}]]
   (if (empty? connected-players)
     (update db :rooms dissoc rid) ;; If there are no more players in the room, delete it.
     (assoc-in db [:rooms rid :players] connected-players))))

(rf/reg-event-db
 :room/close
 (fn [db [_ {rid :rid}]]
   (update db :rooms update rid assoc :closed? true)))

(rf/reg-fx
 ::to-homepage
 (fn [_]   
   (rfe/push-state :router/home)))

(rf/reg-event-fx
 :room/exit
 (fn [_ _]
   {::to-homepage _}))

(rf/reg-event-fx
 :room/enter
 (fn [_ [_ {rid :rid}]]
   {::send-to-room rid}))
