(ns frontend.lobby
  (:require
   [frontend.ws :as ws]
   [re-frame.core :as rf]
   [taoensso.sente :as sente]))

(rf/reg-event-db
 :game/register-all
 (fn [db [_ guids]]
   (assoc db :game/ids guids)))

(defn fetch-games! []
  ((:send-fn ws/client-chsk)
   [:game/get-ids {}]
   1000
   (fn [reply]
     (let [guids (if (sente/cb-success? reply) (:guids reply) #{})]
       (rf/dispatch [:game/register-all guids])))))
