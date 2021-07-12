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
   (if stored-uid
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

(rf/reg-event-fx
 :room/register
 (fn [{db :db} [_ {rid :rid user-id :user-id}]]
   {:db (assoc-in db [:rooms rid] {:host user-id :players #{user-id}})
    ::send-to-room rid}))

(rf/reg-event-db
 :room/update
 (fn [db [_ {rid :rid connected-players :connected-players}]]
   (assoc-in db [:rooms rid :players] connected-players)))
