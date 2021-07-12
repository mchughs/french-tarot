(ns frontend.ws
  (:require
   [re-frame.core :as rf]
   [taoensso.sente :as sente]
   [frontend.events :as ev]))

(def ?csrf-token ;; TODO, won't be working yet.
  (when-let [el (.getElementById js/document "sente-csrf-token")]
    (.getAttribute el "data-csrf-token")))

(defonce client-chsk
  (sente/make-channel-socket-client!
   "/chsk"
   ?csrf-token
   {:type :auto}))

(defmulti event-msg-handler :id)

(defmethod event-msg-handler :default
  [{id :id :as payload}]
  (js/console.log "Unhandled Client Side event:" id payload))

(defmethod event-msg-handler :chsk/recv
  [{event :?data}]
  (let [[event-id ?data] event]
    (rf/dispatch [event-id ?data])
    (js/console.log "serverPush > client" event-id ?data)))

(defmethod event-msg-handler :chsk/handshake
  [{?data :?data}]
  (let [[uid _] ?data]
    (rf/dispatch [::ev/set-uid uid])))

(defmethod event-msg-handler :chsk/state
  [{?data :?data}]
  (let [[_old-state new-state] ?data
        {open? :ever-opened?} new-state]
    (when open?
      (rf/dispatch [::ev/open]))))

;; TODO Other handlers for default sente events...

(defonce ws-router
  (sente/start-client-chsk-router!
   (:ch-recv client-chsk)
   event-msg-handler))

