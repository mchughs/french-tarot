(ns frontend.core
  (:require
   [frontend.events :as ev]
   [frontend.lobby :as lobby]
   [frontend.ws :as ws]
   [frontend.ui.elements.game-lobby-list :as game-lobby-list]
   [re-frame.core :as rf]
   [reagent.dom :as rdom]
   frontend.router))

(defn component []
  [:div
   [:h1 "Welcome to the French Tarrot games lobby."]
   [:div
    [:button {:on-click lobby/fetch-games!}
     "Fetch Existing Games"]
    [:button {:on-click #((:send-fn ws/client-chsk) [:game/create {}])}
     "Create Game"]
    [game-lobby-list/component]]])

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
