(ns frontend.core
  (:require
   [frontend.events :as ev]
   [frontend.lobby :as lobby]
   [frontend.ws :as ws]
   [frontend.ui.elements.game-lobby-list :as game-lobby-list]
   [re-frame.core :as rf]
   [reagent.core :as r]
   [reagent.dom :as rdom]
   frontend.router))

(defn game-room [participants]
  [:div "You're in a game containing " (count participants) "/4 players:"
     [:ul
      (->> participants
           (map (fn [uid]
                  ^{:key (gensym)}
                  [:li (str "Player #" uid)]))
           doall)]])

(defn component []
  (r/with-let [uid (rf/subscribe [::lobby/uid])
               participating-in-game (rf/subscribe [::lobby/participating-in-game])]
    [:div
     [:h1 "Welcome to the game of French Tarrot."]
     [:h2 "Your User ID is " @uid]
     (if @participating-in-game
       [game-room @participating-in-game]
       [:div
        [:button {:on-click lobby/fetch-games!}
         "Fetch Existing Games"]
        [:button {:on-click #((:send-fn ws/client-chsk) [:game/create {:user-id @uid}])}
         "Create Game"]
        [game-lobby-list/component]])]))

(defn init-db! []
  (js/console.log "Init re-frame DB...")
  (rf/dispatch [::ev/load-page]))

(defn mount! []
  (js/console.log "Mounting...")
  (rdom/render [component]
               (js/document.getElementById "app")))

(defn init []
  (js/console.log "Init Frontend...")
  (mount!)
  (init-db!))

(comment
  (init))
