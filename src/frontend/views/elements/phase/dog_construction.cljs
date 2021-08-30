(ns frontend.views.elements.phase.dog-construction
  (:require
   [frontend.controllers.card :as card]
   [frontend.controllers.log :as log]
   [frontend.views.components.dog :as dog]
   [frontend.views.elements.bid-menu :as bid]
   [re-frame.core :as rf]
   [reagent.core :as r]))

(defn component []
  (r/with-let [taker? (rf/subscribe [::log/taker?])
               taker-bid (rf/subscribe [::log/taker-bid])
               init-taker-pile (rf/subscribe [::card/init-taker-pile])]
    [:div.text-white.text-lg
     [:div.pb-2
      "Taker's bid: " [bid/tile @taker-bid] " "
      (when (and @taker?
                 (#{:bid/petit :bid/garde} @taker-bid))
        (let [remaining (- 6 (count @init-taker-pile))]
          (if (zero? remaining)
            [:button.red
             {:on-click #(rf/dispatch [::log/submit-dog])}
             "Submit Dog"]            
            (str "Pick " remaining " more cards to set aside."))))]
     [dog/component]]))
