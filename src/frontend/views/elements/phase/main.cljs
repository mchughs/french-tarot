(ns frontend.views.elements.phase.main
  (:require
   [frontend.controllers.log :as log]
   [frontend.views.components.card :as card-comp]
   [re-frame.core :as rf]
   [reagent.core :as r]))

(defn component []
  (r/with-let [board (rf/subscribe [::log/board])]    
    (when-not (empty? @board)
      [:ul.flex.flex-row.max-w-screen-lg.w-full.flex-wrap
       (->> @board
            (map (fn [{card :board/card
                       play-order :board/play-order}]
                   ^{:key (gensym)}
                   [:<>
                    [:div "ORDER: " play-order]
                    [card-comp/component card]]))
            doall)])))
