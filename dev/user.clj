(ns user
  (:require [clojure.tools.namespace.repl :as repl]
            [mount.core :as mount]
            [mount-up.core :as mu]
            backend.core
            backend.router
            backend.routes.ws))

(mu/on-upndown :info mu/log :before)

(defn start! [& _args]
  (mount/start))

(defn reset [& _args]
  (mount/stop)
  (repl/refresh :after 'user/start!))

(comment
  (start!)
  (reset))
