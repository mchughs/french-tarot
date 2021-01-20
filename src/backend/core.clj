(ns backend.core
  (:require [backend.server :as server]
            [mount.core :refer [defstate]]
            [ring.adapter.jetty :as jetty]))

(defstate my-server
  :start (jetty/run-jetty #'server/app
           {:port  3000
            :join? false})
  :stop (when my-server
          (.stop my-server)))
