(ns frontend.controllers.lobby
  (:require
   [frontend.controllers.room :as room]
   [frontend.controllers.user :as user]
   [frontend.websockets.core :as ws]
   [re-frame.core :as rf]
   [taoensso.sente :as sente]))

(defn fetch-rooms!
  "Fetch all the existing player rooms when the user first visits the site + on explicit re-fetche.s"
  []
  ((:send-fn ws/client-chsk)
   [:rooms/get {}]
   1000
   (fn [reply]
     (let [rooms (if (sente/cb-success? reply) reply {})]
       (rf/dispatch [::room/register-all rooms])))))

(defn fetch-users!
  "Fetch all the existing player rooms when the user first visits the site + on explicit re-fetche.s"
  []
  ((:send-fn ws/client-chsk)
   [:users/get {}]
   1000
   (fn [reply]
     (let [users (if (sente/cb-success? reply) reply {})]
       (rf/dispatch [::user/register-all users])))))
