(ns frontend.views.elements.bid-menu
  (:require
   [frontend.controllers.log :as log]
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
                                         (rf/dispatch [::log/place-bid option]))
                            :disabled (not available-bid?)}
                   option]])))
        doall)])
