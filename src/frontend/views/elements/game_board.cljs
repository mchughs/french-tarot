(ns frontend.views.elements.game-board
  (:require
   [frontend.controllers.log :as log]
   [frontend.controllers.players :as players]
   [frontend.views.components.hand :as hand]
   [frontend.views.elements.phase.announcements :as phase.announcements]
   [frontend.views.elements.phase.bidding :as phase.bidding]
   [frontend.views.elements.phase.dog-construction :as phase.dog-construction]
   [frontend.views.elements.phase.end :as phase.end]
   [frontend.views.elements.phase.main :as phase.main]   
   [frontend.views.elements.phase.scoring :as phase.scoring]
   [frontend.views.elements.player-list :as player-list] ;; TODO will replace eventually
   [re-frame.core :as rf]
   [reagent.core :as r]))

(defn component
  [rid uid {host :room/host players :room/players}]
  (r/with-let [phase (rf/subscribe [::log/phase])
               user-turn? (rf/subscribe [::log/user-turn?])
               score (rf/subscribe [::players/score])]
    [:<>
     (when @user-turn?
       [:h1 "YOUR TURN"])
     [:div "Score:" @score]
     [:div "Phase: " @phase]
     (case @phase
       :bidding [phase.bidding/component]
       :announcements [phase.announcements/component]
       :dog-construction [phase.dog-construction/component]
       :main [phase.main/component]
       :scoring [phase.scoring/component]
       :end [phase.end/component rid uid host]
       [phase.end/component rid uid host])
     [hand/component]
     [player-list/component uid host players]]))
