(ns frontend.views.elements.game-board
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [frontend.controllers.card :as card]
            [frontend.controllers.game :as game]
            [frontend.controllers.log :as log]
            [frontend.controllers.players :as players]            
            [frontend.views.components.dog :as dog]
            [frontend.controllers.round :as round]
            [frontend.views.components.card :as card-comp]
            [frontend.views.components.hand :as hand]
            [frontend.views.elements.bid-menu :as bid-menu]
            [frontend.views.elements.phase.announcements :as phase.announcements]
            [frontend.views.elements.player-list :as player-list] ;; TODO will replace eventually
            ))

(defn component
  [rid uid {host :room/host
            players :room/players
            :as room}]
  (r/with-let [status (rf/subscribe [::game/status])
               phase (rf/subscribe [::log/phase])
               user-turn? (rf/subscribe [::log/user-turn?])
               available-bids (rf/subscribe [::log/available-bids])
               #_#_player-order (rf/subscribe [::players/order])
               taker? (rf/subscribe [::log/taker?])
               taker-bid (rf/subscribe [::log/taker-bid])
               score (rf/subscribe [::players/score])
               init-taker-pile (rf/subscribe [::card/init-taker-pile])
               board (rf/subscribe [::log/board])]
    [:div "game board"
     [:div "Round phase:" @phase]
     [:div "GAmes status " @status]
     #_[:div "Player order " @player-order]
    [:div "Taker's bid:" @taker-bid]
     (when @user-turn?
       [:div "Your turn dude."])

     [:div "PLAYER SCORE:" @score ]

     (when (= :scoring @phase)
       [:div "Scoring"
        [:button {:on-click #(rf/dispatch [::round/score])}
         "Scoring"]])
     
     (when-not (empty? @board)
       [:ul.flex.flex-row.max-w-screen-lg.w-full.flex-wrap
        (->> @board
             (map (fn [{card :board/card
                        play-order :board/play-order}]
                    ^{:key (gensym)}
                    [:<>
                     [:div "ORDER: " play-order]                     
                     [card-comp/component @phase @user-turn? card]]))
             doall)])

     [dog/component]

     (when (and @user-turn?
                (= :bidding @phase)
                @available-bids)
       [bid-menu/component @available-bids])

     (when (and (= :dog-construction @phase)
                @taker?
                (#{:bid/petit :bid/garde} @taker-bid))
       [:div "Pick 6 cards."])

     (when (= :announcements @phase)
       [phase.announcements/component])

     (when (<= 6 (count @init-taker-pile))
       [:div
        {:on-click #(rf/dispatch [::log/submit-dog])}
        "Submit Dog."])

     [hand/component @phase @user-turn?]

     [player-list/component uid host players]

     (when (and (= uid host)
                (= :during @status)
                (= :end @phase))
       [:button {:on-click #(rf/dispatch [::round/end])} ;; DEBUG Just for easy development
        "End the round."])
     (when (and (= uid host)
                (= :pre @status)
                (or (nil? @phase) ;; very first round
                    (= :end @phase)))
       [:button {:on-click #(rf/dispatch [::round/start rid])}
        "Start the round."])]))
