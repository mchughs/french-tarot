(ns user
  (:require [clojure.tools.namespace.repl :as repl]
            [mount.core :as mount]
            [mount-up.core :as mu]
            backend.core))

(mu/on-upndown :info mu/log :before)

(defn start [& args]
  (mount/start))

(defn reset [& args]
  (mount/stop)
  (repl/refresh :after 'user/start))

(start)
