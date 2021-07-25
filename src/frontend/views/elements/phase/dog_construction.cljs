(ns frontend.views.elements.phase.dog-construction
  (:require
   [frontend.controllers.card :as card]
   [frontend.controllers.log :as log]
   [frontend.views.components.dog :as dog]
   [re-frame.core :as rf]
   [reagent.core :as r]))

(defn component []
  (r/with-let [taker? (rf/subscribe [::log/taker?])
               taker-bid (rf/subscribe [::log/taker-bid])
               init-taker-pile (rf/subscribe [::card/init-taker-pile])]
    [:<>
     [:div "Taker's bid:" @taker-bid]
     [dog/component]
     (when (and @taker?
            (#{:bid/petit :bid/garde} @taker-bid))
       [:div "Pick 6 cards."])
     (when (<= 6 (count @init-taker-pile))
       [:div
        {:on-click #(rf/dispatch [::log/submit-dog])}
        "Submit Dog."])]))
