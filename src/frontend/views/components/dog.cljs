(ns frontend.views.components.dog
  (:require
   [frontend.controllers.log :as log]
   [frontend.controllers.round :as round]
   [frontend.views.components.card :as card]
   [re-frame.core :as rf]
   [reagent.core :as r]))

(defn component []
  (r/with-let [dog (rf/subscribe [::log/dog])]
    (when @dog
      [:ul.grid.grid-flow-col.grid-cols-3.grid-rows-2.gap-4
       (->> @dog
            round/sort-cards
            (map (fn [card]
                   ^{:key (gensym)}
                   [card/component card]))
            doall)])))
