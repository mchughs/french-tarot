(ns frontend.views.pages.room-lobby
  (:require
   [frontend.views.elements.player-list :as player-list]
   [frontend.lobby :as lobby]
   [re-frame.core :as rf]
   [reagent.core :as r]))

(defn page [match]
  (let [rid (get-in match [:parameters :path :rid])]
    (r/with-let [uid (rf/subscribe [::lobby/uid])
                 room (rf/subscribe [:room rid])]
      (if-not @room
        [:div "Sorry, we couldn't find a room with ID:" rid]
        (let [{host :host players :players} @room
              host? (= host @uid)]
          [:div
           [:span "You're in a room containing " (count players) "/4 players hosted by " host ":"]
           [player-list/component players]
           [:button {:on-click #(rf/dispatch [::lobby/leave {:user-id @uid :host? host? :rid rid}])}
            "Leave the room."]
           (when (and host?
                      (= 4 (count players)))
             [:button {}
              "Start the game!"])])))))
