(ns frontend.views.elements.phase.announcements
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [frontend.controllers.round :as round]))

(defn component
  [rid]
  (r/with-let [made-announcement? (rf/subscribe [::round/made-announcement?])]
    (if @made-announcement?
      [:div "Waiting on other players..."]
      [:button
       {:on-click #(rf/dispatch [::round/make-announcement {:rid rid :announcements :TODO}])}
       "READY?"]))) ;; TODO, give real options
     