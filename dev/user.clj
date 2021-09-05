(ns user
  (:require [clojure.tools.namespace.repl :as repl]
            [mount.core :as mount]
            [mount-up.core :as mu]
            backend.core
            backend.db
            backend.router
            backend.routes.ws
            prod-ops))

(mu/on-upndown :info mu/log :before)

(defn start! [& _args]
  (mount/start))

(defn reset [& _args]
  (mount/stop)
  (repl/refresh :after 'user/start!))

(defn -main [& _args]
  (start!))

(comment
  (start!)
  (reset))
