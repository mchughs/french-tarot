(ns frontend.views.components.nameplate
  (:require
   [frontend.controllers.players :as players]
   [frontend.controllers.round :as round]
   [frontend.views.components.pile :as pile]
   [re-frame.core :as rf]
   [reagent.core :as r]))

(defn component
  [position-key]
  (r/with-let [player (rf/subscribe [::players/k-player position-key])
               taker-id (rf/subscribe [::players/taker-id])
               player-turn? (rf/subscribe [::players/player-turn? position-key])
               dealer-turn (rf/subscribe [::round/dealer-turn])
               taker-pile (rf/subscribe [::players/taker-pile])
               defenders-pile (rf/subscribe [::players/defenders-pile])]              
    (let [{player-name :player/name
           player-score :player/score
           player-position :player/position
           user-id :player/user-id} @player
          taker? (= @taker-id user-id)
          dealer? (= @dealer-turn player-position)
          pile (if taker? taker-pile defenders-pile)]
      [:div {:class (str "p-2 flex flex-row justify-around w-max rounded"
                         (when @player-turn? " border-8 border-yellow")
                         (if taker?
                           " bg-tertiary "
                           " bg-secondary-500 "))}
       [:div
        [:div.pt-1 player-name
         (when dealer? " (dealer)")]
        [:div.pt-1 "Score: " player-score]]
       [pile/component @pile]])))
