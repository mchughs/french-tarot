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
