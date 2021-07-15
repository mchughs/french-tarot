(ns frontend.websockets.core
  (:require
   [taoensso.sente :as sente]
   [frontend.websockets.event-handler :refer [event-msg-handler]]))

(def ?csrf-token ;; TODO, won't be working yet.
  (when-let [el (.getElementById js/document "sente-csrf-token")]
    (.getAttribute el "data-csrf-token")))

(defonce client-chsk
  (sente/make-channel-socket-client!
   "/chsk"
   ?csrf-token
   {:type :auto}))

(defonce ws-router
  (sente/start-client-chsk-router!
   (:ch-recv client-chsk)
   event-msg-handler))
