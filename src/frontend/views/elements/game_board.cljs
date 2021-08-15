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
   [frontend.views.components.nameplate :as nameplate]
   [re-frame.core :as rf]
   [reagent.core :as r]))

(defn component
  [rid uid {host :room/host}]
  (r/with-let [phase (rf/subscribe [::log/phase])
               user-turn? (rf/subscribe [::log/user-turn?])
               score (rf/subscribe [::players/score])]
    [:div
     (when @user-turn?
       [:h1 "YOUR TURN"])
     [:div "Score:" @score]
     [:div "Phase: " @phase]
     [:div {:class "relative px-6 w-full
                    bg-gradient-to-tr from-green-900 via-green-700 to-green-900"
            :style {:height "800px"}}
      [:div {:class "absolute left-0 top-1/2
                     transform -translate-x-1/2 -translate-y-1/2"}
       [nameplate/component :right]]
      [:div {:class "absolute left-1/2 top-0
                     transform -translate-x-1/2 -translate-y-1/2"}
       [nameplate/component :top]]
      [:div {:class "absolute left-full top-1/2
                     transform -translate-x-1/2 -translate-y-1/2"}
       [nameplate/component :left]]
      [:div {:class "absolute left-1/2 top-full
                     transform -translate-x-1/2 -translate-y-1/2"}
       [nameplate/component :bottom]
       [hand/component]]
      [:div {:class "absolute left-1/2 top-1/2
                     transform -translate-x-1/2 -translate-y-1/2"}
       "Center"
       (case @phase
         :bidding [phase.bidding/component]
         :announcements [phase.announcements/component]
         :dog-construction [phase.dog-construction/component]
         :main [phase.main/component]
         :scoring [phase.scoring/component]
         :end [phase.end/component rid uid host]
         [phase.end/component rid uid host])]]]))
