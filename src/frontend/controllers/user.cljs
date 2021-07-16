(ns frontend.controllers.user
  "For all functions regarding the operating user. Not other users."
  (:require [frontend.controllers.room :as room]
            [re-frame.core :as rf]
            [utils :as utils]))

(rf/reg-sub
 ::id
 (fn [db _]
   (get db :user/id)))

;; Returns the rid for the room the player is in, if any.
;; TODO use cookies to save and make access quicker, or place the rid directly in the db
(rf/reg-sub
 ::room
 :<- [::room/rooms]
 :<- [::id]
 (fn [[rooms uid] _]
   (->> rooms
        (utils/find-first (fn [[_rid {players :players}]]
                            (contains? players uid)))
        first)))

(rf/reg-sub
 ::name ;; Likely to change in implementation.
 (fn [db _]
   (get (:player-map db) (:user/id db))))
