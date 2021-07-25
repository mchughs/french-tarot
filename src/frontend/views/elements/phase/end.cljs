(ns frontend.views.elements.phase.end
  (:require
   [frontend.controllers.round :as round]
   [re-frame.core :as rf]))

(defn component
  [rid uid host]
  (when (= uid host)
    [:button {:on-click #(rf/dispatch [::round/start rid])}
     "Start the round."]))
