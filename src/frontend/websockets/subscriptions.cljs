(ns frontend.websockets.subscriptions
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 ::chsk-open?
 (fn [db _]
   (:chsk/open db)))
