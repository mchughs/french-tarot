(ns frontend.views.pages.room-lobby
  (:require
   [frontend.views.elements.player-list :as player-list]
   [frontend.views.elements.game-board :as game-board]
   [frontend.views.elements.share-section :as share-section]
   [frontend.lobby :as lobby]
   [re-frame.core :as rf]
   [reagent.core :as r]))

(defn page [match]
  (let [rid (get-in match [:parameters :path :rid])]
    (r/with-let [uid (rf/subscribe [::lobby/uid])
                 room (rf/subscribe [:room rid])]
      (cond
        (not @room)
        [:div "Sorry, we couldn't find a room with ID:" rid]

        (:closed? @room)
        [game-board/component rid @uid @room]

        :else
        (let [{host :host players :players} @room
              host? (= host @uid)
              full? (= 4 (count players))
              in?   (contains? players @uid)]
          [:div
           [:div.pt-4.font-medium.text-lg "Players:"]
           [:div.py-4
            [player-list/component @uid host players]]

           (when-not in?
             [:div.w-full.grid.py-6
              [:button.justify-self-center
               {:on-click #(rf/dispatch [::lobby/join rid])}
               "Join"]])

           (when (and host? full?)
             [:button {:on-click #(rf/dispatch [::lobby/start rid])}
              "Start the game!"])

           [share-section/component full?]

           (when in?
             [:div.w-full.grid.py-6
              [:button.red.justify-self-center
               {:on-click #(rf/dispatch [::lobby/leave {:user-id @uid :host? host? :rid rid}])}
               "Leave"]])])))))
