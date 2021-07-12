(ns frontend.views.pages.room-lobby
  (:require 
   [frontend.views.elements.player-list :as player-list]
   [frontend.lobby :as lobby]
   [re-frame.core :as rf]
   [reagent.core :as r]))

(defn page [match]
  (let [rid (get-in match [:parameters :path :rid])]
    (r/with-let [uid (rf/subscribe [::lobby/uid])
                 room (rf/subscribe [:game rid])]
      (if-not @room
        [:div "Sorry, we couldn't find a room with ID:" rid]
        (let [{host :host players :players} @room]
          [:div
           [:span "You're in a game containing " (count players) "/4 players hosted by " host ":"]
           [player-list/component players]
           (when (and (= host @uid)
                      (= 4 (count players)))
             [:button {} "Start the game!"])])))))
