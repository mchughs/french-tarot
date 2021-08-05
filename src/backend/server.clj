(ns backend.server
  (:require
   [backend.config :as config]
   [backend.routes.ws :as routes.ws]
   [compojure.core :as compojure :refer [GET]]
   [compojure.route :as route]
   [hiccup.page :as hiccup.page]
   ;; Middlewares
   ring.middleware.anti-forgery
   ring.middleware.keyword-params
   ring.middleware.params
   ring.middleware.session
   ring.middleware.resource))

(def routes
  (compojure/routes
   (GET "/" []
     (fn [_req]
       ;; TODO Need to figure out how to marry CSRF tokens and shadow-cljs resource-server
       (hiccup.page/html5
        {:lang "en"}
        [:head
         [:title "French Tarot"]
         [:link {:rel "icon" :href (config/get-resource "/icons/icons8-cards-16.png")}]
         [:meta {:charset "UTF-8"}]
         [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
         [:link {:href (config/get-resource "/css/style.css")
                 :rel "stylesheet" :type "text/css"}]
         [:link {:href (config/get-resource "/css/compiled/tailwind.css")
                 :rel "stylesheet" :type "text/css"}]]
        [:body
         [:div#app]
         [:div#sente-csrf-token {:data-csrf-token (force ring.middleware.anti-forgery/*anti-forgery-token*)}]
         [:script {:src (config/get-resource "/js/compiled/app.js")
                   :type "text/javascript"}]])))
   (GET "/status" [] (fn [_req] "OK\n"))
   routes.ws/get-chsk
   routes.ws/post-chsk
   (route/not-found "No such page.")))

(def app
  (-> routes
      ring.middleware.keyword-params/wrap-keyword-params
      ring.middleware.params/wrap-params
      ring.middleware.anti-forgery/wrap-anti-forgery
      ring.middleware.session/wrap-session
      (ring.middleware.resource/wrap-resource "public")))
