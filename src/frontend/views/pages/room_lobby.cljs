(ns frontend.views.pages.room-lobby
  (:require
   [frontend.views.elements.player-list :as player-list]
   [frontend.lobby :as lobby]
   [re-frame.core :as rf]
   [reagent.core :as r]))

(defn page [match]
  (let [rid (get-in match [:parameters :path :rid])]
    (r/with-let [uid (rf/subscribe [::lobby/uid])
                 room (rf/subscribe [:room rid])
                 copied? (r/atom false)]
      (if-not @room
        [:div "Sorry, we couldn't find a room with ID:" rid]
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
             [:button {}
              "Start the game!"])

           (when-not full?
             [:div.grid
              [:div.font-normal.text-md.justify-self-center "Invite a friend by copying the link below"]
              [:button.basic.flex.flex-row.border-4.justify-self-center.relative.mt-2
               {:title (if @copied? "Copied!" "Click to Copy")
                :class (if @copied? "border-green-600" "border-indigo-600")
                :on-click
                #(-> js/navigator
                     .-clipboard
                     (.writeText js/window.location.href)
                     (.then (fn [] (reset! copied? true))
                            (fn [err] (js/alert "Sorry, we experienced an error: " err))))}
               [:div.self-center.pl-1.py-2 js/window.location.href]
               (if-not @copied?
                 [:img.self-center {:src "https://img.icons8.com/dotty/32/000000/clipboard.png"}]
                 [:img.self-center {:src "https://img.icons8.com/color/32/000000/pass.png"}])
               [:div.absolute.self-center.bg-white.bg-opacity-100.w-full.h-full.flex.justify-center.hover:opacity-0
                [:div.self-center "Hover Me!"]]]])

           (when in?
             [:div.w-full.grid.py-6
              [:button.red.justify-self-center
               {:on-click #(rf/dispatch [::lobby/leave {:user-id @uid :host? host? :rid rid}])}
              "Leave"]])])))))
