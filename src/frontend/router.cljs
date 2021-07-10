(ns frontend.router
  (:require
   [frontend.ws :as ws]
   [re-frame.core :as rf]
   [taoensso.sente :as sente]))

(rf/reg-event-db
 :game/register
 (fn [db [_ {guid :guid}]]
   (update db :game/ids conj guid)))

(rf/reg-sub
 :game/ids
 (fn [db _]
   (get db :game/ids)))

(defmulti event-msg-handler :id)

(defmethod event-msg-handler :default
  [{id :id :as payload}]
  (js/console.log "Unhandled Client Side event:" id payload))

(defmethod event-msg-handler :chsk/recv
  [{event :?data}]
  (let [[event-id ?data] event]
    (rf/dispatch [event-id ?data])
    (js/console.log "serverPush > client" event-id ?data)))

;; TODO Other handlers for default sente events...

(defonce router
  (sente/start-client-chsk-router!
   (:ch-recv ws/client-chsk)
   event-msg-handler))
