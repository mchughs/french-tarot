(ns frontend.views.pages.home
  (:require
   [frontend.controllers.lobby :as lobby]
   [frontend.controllers.user :as user]
   [frontend.views.elements.rooms-list :as rooms-list]
   [frontend.websockets.core :as ws]
   [re-frame.core :as rf]
   [reagent.core :as r]))

(defn info-section []
  [:section.mx-auto
   [:p "Welcome to the great game of "
    [:a {:href "https://en.wikipedia.org/wiki/French_Tarot"} "French Tarot"]
    ", not to be confused with the fortune telling activity of "
    [:a {:href "https://en.wikipedia.org/wiki/Tarot_card_reading"} "Tarot card reading"] "."]
   [:div
    [:div.pt-2.text-md.font-normal "Tarot is..."]
    [:ul.list-disc.list-inside
     [:li "a trick taking game"]
     [:li "played over many rounds"]
     [:li "a game requiring 4 players"]
     [:li "played with a special deck of 78 cards"]]]])

(defn page []
  (r/with-let [user-room (rf/subscribe [::user/room])]
    [:div.py-5
     [info-section]
     [rooms-list/component @user-room]
     [:div.flex
      [:button.blue.flex-1.sm:flex-initial.mx-2
       {:on-click lobby/fetch-rooms!}
       "Fetch Existing Rooms"]
      [:button.flex-1.sm:flex-initial.mx-2
       {:on-click #((:send-fn ws/client-chsk) [:room/create {}])
        :disabled @user-room}
       "Host a game"]]]))
