(ns frontend.views.pages.home
  (:require
   [frontend.lobby :as lobby]
   [frontend.views.elements.rooms-list :as rooms-list]
   [frontend.ws :as ws]
   [re-frame.core :as rf]
   [reagent.core :as r]
   frontend.subscriptions))

(defn info-section []
  [:section.mx-auto
   [:p "Welcome to the great game of "
    [:a {:href "https://en.wikipedia.org/wiki/French_Tarot"} "French Tarot"]
    ", not to be confused with the fortune telling activity of "
    [:a {:href "https://en.wikipedia.org/wiki/Tarot_card_reading"} "Tarot card reading"] "."]
   [:h3 "Tarot is..."]
   [:ul.list-disc.list-inside
    [:li "a trick taking game"]
    [:li "played over many rounds"]
    [:li "a game requiring 4 players"]
    [:li "played with a special deck of 78 cards"]]])

(defn page []
  (r/with-let [uid (rf/subscribe [::lobby/uid])
               committed-room (rf/subscribe [:committed-room])]
    [:div.py-5
     [info-section]
     [:div
      [:button {:on-click lobby/fetch-rooms!}
       "Fetch Existing Rooms"]

      [:button {:on-click #((:send-fn ws/client-chsk) [:room/create {:user-id @uid}])
                :disabled @committed-room}
       "Host a game"]]
     [rooms-list/component @committed-room]]))
