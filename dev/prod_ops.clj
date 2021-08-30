(ns prod-ops
  (:require
   [backend.db :as db]
   [backend.models.user :as user]
   [clojure.tools.logging :as log]))

(defn- purge!
  "Intended to be used by SSHing into the prod REPL and running to purge the Crux DB contents.
   Should be on a CRON job or, more ideally, each room/game/round/log should be killed with a ttl.
   Couldn't get this behind an endpoint because of anti-forgery token limitations."
  []
  (log/info "Purging DB...")
  (db/drop-data!)
  (user/clear-users!))

(defn -main [& _args]
  (purge!))

(comment
  (purge!))
