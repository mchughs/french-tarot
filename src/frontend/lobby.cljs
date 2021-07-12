(ns frontend.lobby
  (:require
   [frontend.ws :as ws]
   [re-frame.core :as rf]
   [taoensso.sente :as sente]
   [utils :as utils]))

;; TODO have each user keep track of their ID and use it to determine if they are part of an existing game.

(rf/reg-event-db
 :game/register-all
 (fn [db [_ games]]
   (assoc db :games games)))

(defn fetch-games! []
  ((:send-fn ws/client-chsk)
   [:game/get-ids {}]
   1000
   (fn [reply]
     (let [games (if (sente/cb-success? reply) (:games reply) {})]
       (rf/dispatch [:game/register-all games])))))

(rf/reg-fx
 :game/join
 (fn [[guid user-id]]
   ((:send-fn ws/client-chsk)
    [:game/join {:guid guid :user-id user-id}]
    1000
    (fn [reply]
      (if (sente/cb-success? reply)
        (js/console.log "Got reply " reply)
        (js/alert (str "Oops, you have a problem joining game #" guid "...")))))))

(rf/reg-event-fx
 ::join
 (fn [{db :db} [_ guid]]
   {:game/join [guid (:user/id db)]}))

(rf/reg-sub
 ::uid
 (fn [db _]
   (get db :user/id)))

(rf/reg-sub
 ::participating-in-game
 :<- [::uid]
 :<- [:games]
 (fn [[uid games] _]
   (->> games
        (utils/find-first (fn [[_ {players :players}]] (contains? players uid)))
        second)))
