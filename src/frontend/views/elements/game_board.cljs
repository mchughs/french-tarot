(ns frontend.views.elements.game-board
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [frontend.round :as round]
            [format :as fmt]
            [frontend.views.elements.player-list :as player-list] ;; TODO will replace eventually
            ))

(defn component
  [rid uid {:keys [host players] :as room}]
  (r/with-let [startable? (rf/subscribe [::round/startable?])
               hand #_(r/atom #{{:type :pip, :value 8, :points 0.5, :suit :spades}
                              {:type :pip, :value 10, :points 0.5, :suit :hearts}
                              {:type :pip, :value 2, :points 0.5, :suit :clubs}
                              {:type :trump, :value 28, :points 0.5, :ouder? false}
                              {:type :excuse, :points 4.5, :ouder? true}
                              {:type :trump, :value 30, :points 0.5, :ouder? false}
                              {:type :face, :name :queen, :value 13, :points 3.5, :suit :clubs}
                              {:type :pip, :value 6, :points 0.5, :suit :clubs}
                              {:type :pip, :value 5, :points 0.5, :suit :diamonds}
                              {:type :pip, :value 6, :points 0.5, :suit :diamonds}
                              {:type :trump, :value 19, :points 0.5, :ouder? false}
                              {:type :face, :name :jack, :value 11, :points 1.5, :suit :spades}
                              {:type :face, :name :knight, :value 12, :points 2.5, :suit :spades}
                              {:type :pip, :value 2, :points 0.5, :suit :hearts}
                              {:type :pip, :value 5, :points 0.5, :suit :clubs}
                              {:type :trump, :value 24, :points 0.5, :ouder? false}
                              {:type :trump, :value 31, :points 0.5, :ouder? false}
                              {:type :face, :name :knight, :value 12, :points 2.5, :suit :clubs}})
               (rf/subscribe [::round/hand])]
    [:div "game board"
     (when @hand
       [:ul.flex.flex-row.max-w-screen-lg.w-full.flex-wrap
        (->> @hand
             round/sort-hand
             (map (fn [card]
                    ^{:key (gensym)}
                    [:li [:button (fmt/card->name card)]]))
             doall)])
     [player-list/component uid host players]
     (when (and (= uid host) (not @startable?))
       [:button {:on-click #(rf/dispatch [::round/end rid])} ;; DEBUG Just for easy development
        "End the round."])
     (when (and (= uid host) @startable?)
       [:button {:on-click #(rf/dispatch [::round/start rid])}
        "Start the round."])]))
