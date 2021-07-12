(ns frontend.views.pages.home
  (:require
   [frontend.lobby :as lobby]
   [frontend.views.elements.rooms-list :as rooms-list]
   [frontend.ws :as ws]
   [re-frame.core :as rf]
   [reagent.core :as r]
   frontend.subscriptions))

(defn page []
  (r/with-let [uid (rf/subscribe [::lobby/uid])
               committed-room (rf/subscribe [:committed-room])]
    (if @committed-room ;; TODO definitily a bit jumpy. Could smooth things out.
      ;; TODO should be in a life-cycle  
      (rf/dispatch [:room/enter {:rid @committed-room}]) ;; If the user should be a room, send them there. They can leave if they choose by explicitly exiting.
      [:div
       [:h1 "Welcome to the game of French Tarrot."]
       [:h2 "Your User ID is " @uid]
       [:div
        [:button {:on-click lobby/fetch-rooms!}
         "Fetch Existing Rooms"]
        [:button {:on-click #((:send-fn ws/client-chsk) [:room/create {:user-id @uid}])}
         "Host a game"]
        [rooms-list/component]]])))
