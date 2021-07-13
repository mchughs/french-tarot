(ns frontend.lobby
  (:require
   [frontend.ws :as ws]
   [re-frame.core :as rf]
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

(defn fetch-names! [] ;; For illustrative purposes only
  ((:send-fn ws/client-chsk)
   [:player-name/fetch {}]))

(rf/reg-event-db
 :player-name/publish ;; For illustrative purposes only
 (fn [db [_ {player-map :player-map}]]
   (assoc db :player-map player-map)))

(rf/reg-sub
 :player-name ;; For illustrative purposes only
 (fn [db [_ uid]]
   (get (:player-map db) (or uid (:user/id db)))))

(rf/reg-fx
 :room/join
 (fn [[rid user-id]]
   ((:send-fn ws/client-chsk)
    [:room/join {:rid rid :user-id user-id}]
    1000
    (fn [reply]
      (if (sente/cb-success? reply)
        (rf/dispatch [:room/enter {:rid rid}])
        (js/alert (str "Oops, you have a problem joining room #" rid "...")))))))

(rf/reg-event-fx
 ::join
 (fn [{db :db} [_ rid]]
   {:room/join [rid (:user/id db)]}))

(rf/reg-fx
 :room/leave
 (fn [{:keys [_user-id _host? rid] :as payload}]
   ((:send-fn ws/client-chsk)
    [:room/leave payload]
    1000
    (fn [reply]
      (if (sente/cb-success? reply)
        (rf/dispatch [:room/exit])
        (js/alert (str "Oops, you have a problem leaving room #" rid "...")))))))

(rf/reg-event-fx
 ::leave
 (fn [_ [_ payload]]
   {:room/leave payload}))

(rf/reg-sub
 ::uid
 (fn [db _]
   (get db :user/id)))
