(ns frontend.subscriptions
  (:require [re-frame.core :as rf]
            [frontend.lobby :as lobby]
            [utils :as utils]))

(rf/reg-sub
 :chsk/open?
 (fn [db _]
   (:chsk/open db)))

(rf/reg-sub
 :page/match
 (fn [db _]
   (:page/match db)))

(rf/reg-sub
 :rooms
 (fn [db _]
   (get db :rooms)))

(rf/reg-sub
 :room
 (fn [db [_ rid]]
   (get-in db [:rooms rid])))

(rf/reg-sub
 :committed-room ;; The rid for the room the player is in, if any.
 :<- [:rooms]
 :<- [::lobby/uid]
 (fn [[rooms uid] _]
   (->> rooms
        (utils/find-first (fn [[_rid {players :players}]]
                            (contains? players uid)))
        first)))
