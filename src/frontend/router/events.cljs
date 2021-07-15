(ns frontend.router.events
  (:require
   [re-frame.core :as rf]            
   [reitit.frontend.easy :as rfe]))

(rf/reg-event-db
 ::update-page-match
 (fn [db [_ match]]
   (assoc db :page/match match)))

(rf/reg-fx
 ::to-homepage
 (fn [_]
   (rfe/push-state :router/home)))

(rf/reg-fx
 ::to-roompage
 (fn [rid]
   (rfe/push-state :router/room-lobby {:rid rid})))
