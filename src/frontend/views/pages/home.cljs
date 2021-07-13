(ns frontend.views.pages.home
  (:require
   [frontend.lobby :as lobby]
   [frontend.views.elements.rooms-list :as rooms-list]
   [frontend.ws :as ws]
   [re-frame.core :as rf]
   [reagent.core :as r]
   [frontend.views.components.button :as button]
   frontend.subscriptions))

(defn info-section []
  [:section
   [:p "Welcome to the great game of "
    [:a.underline {:href "https://en.wikipedia.org/wiki/French_Tarot"} "French Tarot"]
    ", not to be confused with the fortune telling activity of "
    [:a.underline {:href "https://en.wikipedia.org/wiki/Tarot_card_reading"} "Tarot card reading"] "."]
   [:h3 "Tarot is..."]
   [:ul
    [:li "a trick taking game"]
    [:li "played over many rounds"]
    [:li "a game requiring 4 players"]]])

(defn page []
  (r/with-let [uid (rf/subscribe [::lobby/uid])
               committed-room (rf/subscribe [:committed-room])]
    [:div {:class "py-4"}
     [info-section]
     [:div
      [button/component {:on-click lobby/fetch-rooms!}
       "Fetch Existing Rooms"]
      
      [button/component {:on-click #((:send-fn ws/client-chsk) [:room/create {:user-id @uid}])
                         :disabled @committed-room}
       "Host a game"]]
     [rooms-list/component @committed-room]]))
