(ns frontend.cookies
  (:require [re-frame.core :as rf]
            [reagent.cookies :as rc]))

(rf/reg-fx
 ::set-uid
 (fn [uid]
   (rc/set! :sente-user-id uid)))

(rf/reg-cofx
 ::uid
 (fn [coeffects _]
   (if-let [stored-id (rc/get :sente-user-id)]
     (assoc coeffects :cookie/uid stored-id)
     coeffects)))

(rf/reg-fx
 ::set-username
 (fn [username]
   (rc/set! :sente-username username)))

(rf/reg-cofx
 ::username
 (fn [coeffects _]
   (if-let [stored-username (rc/get :sente-username)]
     (assoc coeffects :cookie/username stored-username)
     coeffects)))
