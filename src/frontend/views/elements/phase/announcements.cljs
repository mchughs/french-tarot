(ns frontend.views.elements.phase.announcements
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [frontend.controllers.log :as log]))

(defn component
  [rid]
  (r/with-let [made-announcement? (rf/subscribe [::log/made-announcement?])]
    (if @made-announcement?
      [:div "Waiting on other players..."]
      [:button
       {:on-click #(rf/dispatch [::log/make-announcement :TODO])}
       "READY?"]))) ;; TODO, give real options
     