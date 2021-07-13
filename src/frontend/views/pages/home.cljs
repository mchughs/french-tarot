(ns frontend.views.pages.home
  (:require
   [frontend.lobby :as lobby]
   [frontend.views.elements.rooms-list :as rooms-list]
   [frontend.ws :as ws]
   [re-frame.core :as rf]
   [reagent.core :as r]
   [frontend.views.components.button :as button]
   frontend.subscriptions))

(defn page []
  (r/with-let [uid (rf/subscribe [::lobby/uid])
               committed-room (rf/subscribe [:committed-room])]
    [:div
     [:div "You are already in room " @committed-room ". Click on the room tab to return to it"]
     [button/component {:on-click lobby/fetch-rooms!}
      "Fetch Existing Rooms"]
     [button/component {:on-click #((:send-fn ws/client-chsk) [:room/create {:user-id @uid}])
                        :disabled @committed-room}
      "Host a game"]
     [rooms-list/component]]))
