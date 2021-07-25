(ns frontend.views.elements.phase.bidding
  (:require
   [frontend.controllers.log :as log]
   [frontend.views.elements.bid-menu :as bid-menu]
   [re-frame.core :as rf]
   [reagent.core :as r]))

(defn component []
  (r/with-let [user-turn? (rf/subscribe [::log/user-turn?])
               available-bids (rf/subscribe [::log/available-bids])]
    (when (and @user-turn? @available-bids)
       [bid-menu/component @available-bids])))
