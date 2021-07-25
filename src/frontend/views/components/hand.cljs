(ns frontend.views.components.hand
  (:require
   [frontend.controllers.players :as players]
   [frontend.controllers.round :as round]
   [frontend.views.components.card :as card]
   [re-frame.core :as rf]
   [reagent.core :as r]))

(defn component []
  (r/with-let [hand (rf/subscribe [::players/hand])]
    (when @hand
      [:ul.flex.flex-row.max-w-screen-lg.w-full.flex-wrap
       (->> @hand
            round/sort-cards
            (map (fn [card]
                   ^{:key (gensym)}
                   [card/component card]))
            doall)])))
