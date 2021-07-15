(ns frontend.router.subscriptions
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 ::page-match
 (fn [db _]
   (:page/match db)))
