(ns frontend.core
  (:require
   [frontend.events :as ev]
   [frontend.lobby :as lobby]
   frontend.subscriptions
   [frontend.ws :as ws]
   [frontend.ui.elements.game-lobby-list :as game-lobby-list]
   [frontend.ui.elements.player-list :as player-list]
   [re-frame.core :as rf]
   [reagent.core :as r]
   [reagent.dom :as rdom]
   frontend.router))

(defn game-room [uid {host :host players :players}]
  [:div "You're in a game containing " (count players) "/4 players hosted by " host ":"
   [player-list/component players]
   (when (and (= host uid)
              (= 4 (count players)))
     [:button {} "Start the game!"])])

(defn component []
  (r/with-let [open (rf/subscribe [:chsk/open?])
               uid (rf/subscribe [::lobby/uid])
               participating-in-game (rf/subscribe [::lobby/participating-in-game])]
    (when @open ;; We need to wait for some data from the server coming over the socket for the initial render.
      [:div
       [:h1 "Welcome to the game of French Tarrot."]
       [:h2 "Your User ID is " @uid]
       (if @participating-in-game
         [game-room @uid @participating-in-game]
         [:div
          [:button {:on-click lobby/fetch-games!}
           "Fetch Existing Games"]
          [:button {:on-click #((:send-fn ws/client-chsk) [:game/create {:user-id @uid}])}
           "Host a game"]
          [game-lobby-list/component]])])))

(defn init-db! []
  (js/console.log "Init re-frame DB...")
  (rf/dispatch [::ev/load-page]))

(defn mount! []
  (js/console.log "Mounting...")
  (rdom/render [component]
               (js/document.getElementById "app")))

(defn add-channel-socket-watcher! []
  (add-watch
   (rf/subscribe [:chsk/open?]) ;; Needs to be deref somewhere later on or the callback is never called.
   :chsk/open
   (fn [_key _atom old-state new-state]
     (when (and (not= old-state new-state) new-state)
       (lobby/fetch-games!)))))

(defn init []
  (js/console.log "Init Frontend...")
  (init-db!)
  (add-channel-socket-watcher!)
  (mount!))

(comment
  (init))
