(ns frontend.views.components.nameplate
  (:require
   [frontend.controllers.players :as players]
   [frontend.controllers.round :as round]
   [re-frame.core :as rf]
   [reagent.core :as r]))

(defn component
  [position-key]
  (r/with-let [player (rf/subscribe [::players/k-player position-key])
               player-turn? (rf/subscribe [::players/player-turn? position-key])
               dealer-turn (rf/subscribe [::round/dealer-turn])]
    (let [{player-name :player/name
           player-score :player/score
           player-position :player/position} @player]
      [:div {:class (str "bg-gray-300 p-2 "
                         (when @player-turn? "border-8 border-indigo-600"))}
       [:div player-name (when (= @dealer-turn
                                  player-position)
                           " (dealer)")]
       [:div "Score: " player-score]])))
