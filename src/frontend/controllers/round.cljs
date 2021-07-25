(ns frontend.controllers.round
  (:require
   [clojure.set :as set]
   [frontend.websockets.core :as ws]
   [re-frame.core :as rf]
   [taoensso.sente :as sente]
   [utils :as utils]))

(rf/reg-fx
 :round/start
 (fn [{_rid :rid :as payload}]
   ((:send-fn ws/client-chsk)
    [:round/start payload]
    1000
    (fn [reply]
      (if (sente/cb-success? reply)
        (js/console.log "Success: started the round")
        (js/alert (str "Oops, you have a problem starting the round " reply "...")))))))

(rf/reg-event-db
 ::update
 (fn [db [_ round]]   
   (-> db
       (assoc :prev/round (:curr/round db))
       (assoc :curr/round round))))

(rf/reg-event-fx
 ::start
 (fn [{db :db} [_ rid]]
   {:round/start {:rid rid
                  :gid (get-in db [:game :game/id])}}))

(defn sort-cards [cards]
  (let [sorted-ish (->> cards
                        (group-by :suit)
                        (utils/fmap (partial sort-by :value)))
        {:keys [spades
                hearts
                clubs
                diamonds
                trumps]} (set/rename-keys sorted-ish {nil :trumps})]
    (concat spades hearts clubs diamonds trumps)))

(rf/reg-fx
 :round/score
 (fn [payload]
   ((:send-fn ws/client-chsk)
    [:round/score payload]
    1000
    (fn [reply]
      (if (sente/cb-success? reply)
        (js/console.log "Success: made round/score")
        (js/alert (str "Oops, you have a problem making the round/score " reply "...")))))))

(rf/reg-event-fx
 ::score
 (fn [{db :db} _]
   {:round/score {:log-id (get-in db [:curr/log :log/id])
                  :gid (get-in db [:game :game/id])}}))
