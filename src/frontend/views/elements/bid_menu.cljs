(ns frontend.views.elements.bid-menu
  (:require
   [frontend.controllers.round :as round]
   [frontend.controllers.user :as user]
   [re-frame.core :as rf]))

(def bid-options
  [:bid/petit
   :bid/garde
   :bid/garde-sans
   :bid/garde-contre
   :pass])

(defn component [available-bids]
  [:ul
   (->> bid-options
        (map (fn [option]
                ^{:key (gensym)}
               (let [available-bid? (or (= :pass option) ;; passing is always available
                                        (contains? (set available-bids)
                                                   option))]
                 [:li
                  [:button {:on-click #(when available-bid?
                                         (rf/dispatch [::round/place-bid
                                                       @(rf/subscribe [::user/room]) ;; TODO should be accessed in the dispatch itself
                                                       option]))
                            :disabled (not available-bid?)}
                   option]])))
        doall)])
