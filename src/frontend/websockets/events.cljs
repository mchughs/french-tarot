(ns frontend.websockets.events
  (:require [re-frame.core :as rf]
            [frontend.cookies :as cookies]))

(rf/reg-event-fx
 ::set-user-id
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
