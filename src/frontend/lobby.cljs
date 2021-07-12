(ns frontend.lobby
  (:require
   [frontend.ws :as ws]
   [re-frame.core :as rf]
   [reitit.frontend.easy :as rfe]
   [taoensso.sente :as sente]))

;; TODO have each user keep track of their ID and use it to determine if they are part of an existing room.

(rf/reg-event-db
 :room/register-all
 (fn [db [_ rooms]]
   (assoc db :rooms rooms)))

(defn fetch-rooms! []
  ((:send-fn ws/client-chsk)
   [:room/get-ids {}]
   1000
   (fn [reply]
     (let [rooms (if (sente/cb-success? reply) (:rooms reply) {})]
       (rf/dispatch [:room/register-all rooms])))))

(rf/reg-fx
 :room/join
 (fn [[rid user-id]]
   ((:send-fn ws/client-chsk)
    [:room/join {:rid rid :user-id user-id}]
    1000
    (fn [reply]
      (if (sente/cb-success? reply)
        (rfe/push-state :router/room-lobby {:rid rid})
        (js/alert (str "Oops, you have a problem joining room #" rid "...")))))))

(rf/reg-event-fx
 ::join
 (fn [{db :db} [_ rid]]
   {:room/join [rid (:user/id db)]}))

(rf/reg-sub
 ::uid
 (fn [db _]
   (get db :user/id)))
