(ns frontend.router
  (:require
   [frontend.events :as ev]
   [frontend.views.pages.home :as home]
   [frontend.views.pages.room-lobby :as room-lobby]
   [re-frame.core :as rf]
   [reitit.coercion.spec :as reitit.s.s]
   [reitit.frontend :as reitit.f]
   [reitit.frontend.easy :as reitit.f.e]))

(def routes
  [["/"
    {:name :router/home
     :view home/page}]

   ["/room-lobby/:rid"
    {:name :router/room-lobby
     :view room-lobby/page
     :parameters {:path {:rid uuid?}}}]])

(defn- on-navigation-fn [new-match]
  (rf/dispatch [::ev/update-page-match new-match]))

(defn start! []
  (js/console.log "Starting Router...")
  (reitit.f.e/start!
   (reitit.f/router routes {:data {:coercion reitit.s.s/coercion}})
   on-navigation-fn
    ;; set to false to enable HistoryAPI
   {:use-fragment true}))
