(ns frontend.websockets.event-handler
  (:require
   [re-frame.core :as rf]
   [frontend.websockets.events :as ws.events]))

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
    (rf/dispatch-sync [::ws.events/set-user-id uid])))

(defmethod event-msg-handler :chsk/state
  [{?data :?data}]
  (let [[_old-state new-state] ?data
        {open? :ever-opened?} new-state]
    (when open?
      (rf/dispatch [::ws.events/open]))))

;; TODO Other handlers for default sente events...
