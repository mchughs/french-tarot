(ns backend.router
  (:require
   [backend.routes.ws :as ws]
   [clj-uuid :as uuid]
   [clojure.tools.logging :as log]
   [mount.core :refer [defstate]]
   [taoensso.sente :as sente]
   [utils :as utils]))

(defn- broadcast! [event]
  (doseq [uid (:any @(:connected-uids ws/server-chsk))]
    ((:send-fn ws/server-chsk) uid event)))

(defmulti event-msg-handler :id)

(defmethod event-msg-handler :default
  [{:keys [_event id _?data _ring-req _?reply-fn _send-fn] :as payload}]
  (log/infof "Unhandled event: %s with payload\n%s" id (utils/pp-str payload)))

;; TODO better persistence
(defonce registered-games (atom #{}))

(defmethod event-msg-handler :game/create
  [_]
  (let [guid (uuid/v4)]
    (swap! registered-games conj guid)
    (broadcast! [:game/register {:guid guid}])))

(defmethod event-msg-handler :game/get-ids
  [{:keys [?reply-fn]}]
  (when ?reply-fn
    (?reply-fn {:guids @registered-games})))

;; TODO Other handlers for default sente events...

(defmethod event-msg-handler :chsk/uidport-open
  [{?data :?data}]
  (log/info "Server " (pr-str ?data)))

(defstate router
  :start (sente/start-server-chsk-router!
          (:ch-recv ws/server-chsk)
          event-msg-handler))
