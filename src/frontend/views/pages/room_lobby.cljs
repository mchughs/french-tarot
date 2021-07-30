(ns frontend.views.pages.room-lobby
  (:require
   [frontend.controllers.game :as game]
   [frontend.controllers.room :as room]
   [frontend.controllers.user :as user]
   [frontend.views.elements.game-board :as game-board]
   [frontend.views.elements.player-list :as player-list]
   [frontend.views.elements.share-section :as share-section]
   [re-frame.core :as rf]
   [reagent.core :as r]))

(defn page [match]
  (let [rid (get-in match [:parameters :path :rid])]
    (r/with-let [uid (rf/subscribe [::user/id])
                 room (rf/subscribe [::room/room rid])]
      (cond
        (not @room)
        [:div "Sorry, we couldn't find a room with ID:" rid]

        (= :closed (:room/status @room))
        [game-board/component rid @uid @room]

        :else
        (let [{host        :room/host
               players     :room/players
               playernames :room/playernames
               status      :room/status} @room
              host?    (= host @uid)
              full?    (= :full status)
              in?      (contains? players @uid)]
          [:div
           [:div.pt-4.font-medium.text-lg "Players:"]
           [:div.py-4
            [player-list/component @uid host players playernames]]

           (when-not in?
             [:div.w-full.grid.py-6
              [:button.justify-self-center
               {:on-click #(rf/dispatch [::room/join rid])}
               "Join"]])

           (when (and host? full?)
             [:button {:on-click #(rf/dispatch [::game/start rid])}
              "Start the game!"])

           [share-section/component full?]

           (when in?
             [:div.w-full.grid.py-6
              [:button.red.justify-self-center
               {:on-click #(rf/dispatch [::room/leave {:user-id @uid :host? host? :rid rid}])}
               "Leave"]])])))))
