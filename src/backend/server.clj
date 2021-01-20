(ns backend.server
  (:require [backend.routes.swagger          :as routes.swagger]
            [muuntaja.core                   :as m]
            [reitit.ring                     :as ring]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.swagger                  :as swagger]
            [reitit.swagger-ui               :as swagger-ui]))

(defn wrap [handler id]
  (fn [request]
    (update (handler request) :wrap (fnil conj '()) id)))

(def router-config
  {:data {:muuntaja   m/instance
          :middleware [swagger/swagger-feature
                       muuntaja/format-middleware]}})

(def app
  (ring/ring-handler
    (ring/router
      [routes.swagger/swag
       ["/api" {:middleware [[wrap :api]]}
        ["/start" {:name :game/start
                   :get (fn [_] {:status 200
                                 :body "Starting game..."})}]
        ["/end" {:name :game/end
                 :get (fn [_] {:status 200
                               :body "Ending game..."})}]]]
      router-config)
    (ring/routes
      (swagger-ui/create-swagger-ui-handler {:path "/"})
      (ring/create-default-handler))))
