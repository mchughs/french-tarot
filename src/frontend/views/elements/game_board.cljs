(ns frontend.views.elements.game-board
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [frontend.controllers.card :as card]
            [frontend.controllers.players :as players]
            [frontend.controllers.round :as round]
            [frontend.views.components.card :as card-comp]
            [format :as fmt]
            [frontend.views.elements.player-list :as player-list] ;; TODO will replace eventually
            [frontend.views.elements.bid-menu :as bid-menu]))

(defn component
  [rid uid {:keys [host players game-status] :as room}]
  (r/with-let [hand (rf/subscribe [::round/hand])
               phase (rf/subscribe [::round/phase])
               user-turn? (rf/subscribe [::round/user-turn?])
               available-bids (rf/subscribe [::round/available-bids])
               player-order (rf/subscribe [::players/order])
               taker? (rf/subscribe [::round/taker?])
               taker-bid (rf/subscribe [::round/taker-bid])
               made-announcement? (rf/subscribe [::round/made-announcement?])
               init-taker-pile (rf/subscribe [::card/init-taker-pile])]
    [:div "game board"
     [:div "Round phase:" @phase]
     [:div "Player order " @player-order]
     [:div "Taker's bid:" @taker-bid]

     (when (and @user-turn?
                (= :bidding @phase)
                @available-bids)
       [bid-menu/component @available-bids])
     
     (when (and (= :dog-construction @phase)
                @taker?
                (#{:bid/petit :bid/garde} @taker-bid))
       [:div "Pick 6 cards."])

     (when (= :announcements @phase)
       (if @made-announcement?
         [:div "Waiting on other players..."]
         [:button
          {:on-click #(rf/dispatch [::round/make-announcement {:rid rid :announcements :TODO}])}
          "READY?"])) ;; TODO, give real options
     
     (when (<= 6 (count @init-taker-pile))
       [:div
        {:on-click #(rf/dispatch [::round/submit-dog {:rid rid}])}
        "Submit Dog."])
     
     (when @hand
       [:ul.flex.flex-row.max-w-screen-lg.w-full.flex-wrap
        (->> @hand
             round/sort-hand
             (map (fn [card]
                    ^{:key (gensym)}
                    [card-comp/component @phase @taker? @init-taker-pile card]))
             doall)])
     
     [player-list/component uid host players]

     (when (and (= uid host)
                (= :playing-round game-status)
                (= :end @phase))
       [:button {:on-click #(rf/dispatch [::round/end rid])} ;; DEBUG Just for easy development
        "End the round."])
     (when (and (= uid host)
                (= :in-progress game-status)
                (or (nil? @phase) ;; very first round
                    (= :end @phase)))
       [:button {:on-click #(rf/dispatch [::round/start rid])}
        "Start the round."])]))
