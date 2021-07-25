(ns frontend.views.elements.phase.scoring
  (:require   
   [frontend.controllers.round :as round]   
   [re-frame.core :as rf]))

(defn component []
  [:button {:on-click #(rf/dispatch [::round/score])}
   "Scoring"])
