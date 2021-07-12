(ns frontend.subscriptions
  (:require [re-frame.core :as rf]))

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
 (fn [db [_ guid]]
   (get-in db [:rooms guid])))
