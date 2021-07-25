(ns frontend.controllers.log
  (:require
   [re-frame.core :as rf]
   [frontend.controllers.players :as players]
   [frontend.websockets.core :as ws]
   [taoensso.sente :as sente]))

(rf/reg-event-db
 ::update
 (fn [db [_ log]]   
   (-> db
       (assoc :prev/log (:curr/log db))
       (assoc :curr/log log))))

(rf/reg-sub
 ::log
 (fn [db _]
   (:curr/log db)))

(rf/reg-sub
 ::phase
 :<- [::log]
 (fn [log _]
   (:log/phase log)))

(rf/reg-sub
 ::user-turn?
 :<- [::log]
 :<- [::players/player]
 (fn [[log player] _]
   (= (:player/position player)
      (:log/player-turn log))))

(rf/reg-sub
 ::available-bids
 :<- [::log]
 (fn [log _]
   (:log/available-bids log)))

(rf/reg-sub
 ::dog
 :<- [::log]
 (fn [log _]
   (:log/dog log)))

(rf/reg-sub
 ::taker?
 :<- [::log]
 :<- [::players/player]
 (fn [[log player] _]
   (= (get-in log [:log/taker :uid])
      (:player/user-id player))))

(rf/reg-sub
 ::taker-bid
 :<- [::log] 
 (fn [log _]
   (get-in log [:log/taker :bid])))

(rf/reg-sub
 ::board
 :<- [::log]
 (fn [log _]
   (:log/board log)))

;; events

(rf/reg-fx
 :log/place-bid
 (fn [payload]
   ((:send-fn ws/client-chsk)
    [:log/place-bid payload]
    1000
    (fn [reply]
      (if (sente/cb-success? reply)
        (js/console.log "Success: placed-bid")
        (js/alert (str "Oops, you have a problem placing the bid " reply "...")))))))

(rf/reg-event-fx
 ::place-bid
 (fn [{db :db} [_ bid]]
   {:log/place-bid {:bid bid
                    :gid (get-in db [:game :game/id])
                    :log-id (get-in db [:curr/log :log/id])}}))

(rf/reg-fx
 :log/make-announcement
 (fn [payload]
   ((:send-fn ws/client-chsk)
    [:log/make-announcement payload]
    1000
    (fn [reply]
      (if (sente/cb-success? reply)
        (js/console.log "Success: made announcement")
        (js/alert (str "Oops, you have a problem making the announcement " reply "...")))))))

(rf/reg-event-fx
 ::make-announcement
 (fn [{db :db} [_ announcement]]
   {:log/make-announcement {:announcement announcement
                            :log-id (get-in db [:curr/log :log/id])}}))

(rf/reg-sub
 ::made-announcement?
 :<- [::log]
 :<- [::players/player]
 (fn [[log player] _]
   (contains? (:log/announcements log)
              (:player/user-id player))))

(rf/reg-event-db
 ::start-main
 (fn [db _]
   (dissoc db :init-taker-pile)))

(rf/reg-fx
 :log/submit-dog
 (fn [payload]
   ((:send-fn ws/client-chsk)
    [:log/submit-dog payload]
    1000
    (fn [reply]
      (if (sente/cb-success? reply)
        (rf/dispatch [::start-main])
        (js/alert (str "Oops, you have a problem making the submit-dog " reply "...")))))))

(rf/reg-event-fx
 ::submit-dog
 (fn [{db :db} _]
   {:log/submit-dog {:log-id (get-in db [:curr/log :log/id])
                     :init-taker-pile (:init-taker-pile db)}}))
