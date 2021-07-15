(ns frontend.controllers.lobby
  (:require
   [frontend.controllers.room :as room]
   [frontend.websockets.core :as ws]
   [re-frame.core :as rf]
   [taoensso.sente :as sente]))

(defn fetch-rooms!
  "Fetch all the existing player rooms when the user first visits the site + on explicit re-fetche.s"
  []
  ((:send-fn ws/client-chsk)
   [:room/get-ids {}]
   1000
   (fn [reply]
     (let [rooms (if (sente/cb-success? reply) (:rooms reply) {})]
       (rf/dispatch [::room/register-all rooms])))))

(rf/reg-event-db
 :player-name/publish ;; For illustrative purposes only
 (fn [db [_ {player-map :player-map}]]
   (assoc db :player-map player-map)))

(defn fetch-names! [] ;; For illustrative purposes only
  ((:send-fn ws/client-chsk)
   [:player-name/fetch {}]))
