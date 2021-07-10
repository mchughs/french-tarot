(ns frontend.events
  (:require [re-frame.core :as rf]))

(rf/reg-event-db
 ::load-page
 ;; TODO initialize rf-db data for a client.
 (fn [db _] db))
