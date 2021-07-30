(ns frontend.websockets.events
  (:require [re-frame.core :as rf]
            [frontend.cookies :as cookies]))

(rf/reg-event-fx
 ::set-user-id
 [(rf/inject-cofx ::cookies/uid)
  (rf/inject-cofx ::cookies/username)]
 (fn [{db :db
       stored-uid :cookie/uid
       stored-username :cookie/username} [_ uid]]
   (merge
    {:db (cond-> db
           stored-username (assoc :user/name stored-username)
           true            (assoc :user/id (or stored-uid uid)))}
    (when-not stored-uid
      {::cookies/set-uid uid}))))

(rf/reg-event-db
 ::open
 (fn [db _]
   (assoc db :chsk/open true)))
