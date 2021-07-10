(ns backend.core
  (:require
   [aleph.http :as http]
   [backend.server :as server]
   [mount.core :refer [defstate]]))

(defstate my-server
  :start (http/start-server #'server/app
                            {:port  3000
                             :join? false})
  :stop (when my-server
          (.close my-server)))
