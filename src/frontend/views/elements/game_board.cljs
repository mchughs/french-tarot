(ns frontend.views.elements.game-board
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [frontend.controllers.round :as round]
            [format :as fmt]
            [frontend.views.elements.player-list :as player-list] ;; TODO will replace eventually
            [frontend.views.elements.bid-menu :as bid-menu]))

(def example-hand
  #{{:type :pip, :value 8, :points 0.5, :suit :spades}
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

(defn component
  [rid uid {:keys [host players game-status] :as room}]
  (r/with-let [hand #_(r/atom example-hand) (rf/subscribe [::round/hand])
               phase (rf/subscribe [::round/phase])
               user-turn? (rf/subscribe [::round/user-turn?])
               available-bids (rf/subscribe [::round/available-bids])]
    [:div "game board"
     [:div "Round phase:" @phase]
     (when (and @user-turn?
                (= :bidding @phase)
                @available-bids)
       [bid-menu/component @available-bids])
     (when @hand
       [:ul.flex.flex-row.max-w-screen-lg.w-full.flex-wrap
        (->> @hand
             round/sort-hand
             (map (fn [card]
                    ^{:key (gensym)}
                    [:li [:button (fmt/card->name card)]]))
             doall)])
     [player-list/component uid host players]
     (when (and (= uid host) (= :playing-round game-status))
       [:button {:on-click #(rf/dispatch [::round/end rid])} ;; DEBUG Just for easy development
        "End the round."])
     (when (and (= uid host) (= :in-progress game-status))
       [:button {:on-click #(rf/dispatch [::round/start rid])}
        "Start the round."])]))
