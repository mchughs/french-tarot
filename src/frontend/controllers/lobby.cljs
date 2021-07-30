(ns frontend.controllers.lobby
  (:require
   [frontend.controllers.user :as user]
   [frontend.controllers.room :as room]
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

(defn fetch-user!
  "Fetch all the existing user data visits the site + on explicit re-fetche.s"
  []
  ((:send-fn ws/client-chsk)
   [:user/get {}]
   1000
   (fn [reply]
     (when (sente/cb-success? reply)
       (rf/dispatch [::user/update reply])))))
