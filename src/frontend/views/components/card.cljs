(ns frontend.views.components.card
  (:require
   [reagent.core :as r]
   [re-frame.core :as rf]
   [frontend.controllers.card :as card]
   [clojure.string :as s]
   [format :as fmt]))

(defn component [phase taker? init-taker-pile user-turn? rid card]
  (r/with-let [#_#_set-aside-cards (rf/subscribe [::card/init-pile])]
    [:li
     [:button {:class (when (contains? init-taker-pile card) "blue")
               :disabled (and (<= 6 (count init-taker-pile))
                              (not (contains? init-taker-pile card)))
               :on-click (cond (and (= :dog-construction phase)
                                    taker?)
                           (if (contains? init-taker-pile card)
                             #(rf/dispatch [::card/recover card])
                             #(rf/dispatch [::card/set-aside card]))
                          (and (= :main phase)
                               user-turn?)
                               #(rf/dispatch [::card/play rid card]))}
      [:img {:src (fmt/fmt "http://localhost:5444/assets/images/cards/%s.jpg" (s/replace (fmt/card->name card) #"\s" "_"))
             :title (fmt/card->name card)}]]]))
