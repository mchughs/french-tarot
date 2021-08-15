(ns frontend.views.elements.phase.main
  (:require
   [frontend.controllers.log :as log]
   [frontend.views.components.card :as card-comp]
   [re-frame.core :as rf]
   [reagent.core :as r]))

(defn component []
  (r/with-let [board (rf/subscribe [::log/board])]
    (let [{:keys [right top left bottom]} @board]
      [:div.play-area
       {:class "relative"
        :style {:width "300px"
                :height "300px"}}
       [:div {:class "absolute left-0 top-1/2
                      transform -translate-x-1/2 -translate-y-1/2"}
        (if right
          [card-comp/component right]
          [card-comp/back])]
       [:div {:class "absolute left-1/2 top-0
                      transform -translate-x-1/2 -translate-y-1/2"}
        (if top
          [card-comp/component top]
          [card-comp/back])]
       [:div {:class "absolute left-full top-1/2
                      transform -translate-x-1/2 -translate-y-1/2"}
        (if left
          [card-comp/component left]
          [card-comp/back])]
       [:div {:class "absolute left-1/2 top-full
                      transform -translate-x-1/2 -translate-y-1/2"}
        (if bottom
          [card-comp/component bottom]
          [card-comp/back])]])))
