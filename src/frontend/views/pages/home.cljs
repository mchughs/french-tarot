(ns frontend.views.pages.home
  (:require
   [frontend.lobby :as lobby]
   [frontend.views.elements.game-lobby-list :as game-lobby-list]
   [frontend.ws :as ws]
   [re-frame.core :as rf]
   [reagent.core :as r]
   frontend.subscriptions))

(defn page []
  (r/with-let [uid (rf/subscribe [::lobby/uid])]
    [:div
     [:h1 "Welcome to the game of French Tarrot."]
     [:h2 "Your User ID is " @uid]
     [:div
      [:button {:on-click lobby/fetch-games!}
       "Fetch Existing Games"]
      [:button {:on-click #((:send-fn ws/client-chsk) [:game/create {:user-id @uid}])}
       "Host a game"]
      [game-lobby-list/component]]]))
