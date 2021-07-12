(ns frontend.events
  (:require [re-frame.core :as rf]
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
