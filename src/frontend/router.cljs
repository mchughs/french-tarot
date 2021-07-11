(ns frontend.router
  (:require
   [frontend.ws :as ws]
   [re-frame.core :as rf]
   [taoensso.sente :as sente]
   [frontend.events :as ev]))

(rf/reg-event-db
 :game/register
 (fn [db [_ {guid :guid user-id :user-id}]]
   (assoc-in db [:games guid] #{user-id})))

(rf/reg-event-db
 :game/update
 (fn [db [_ {guid :guid connected-players :connected-players}]]
   (assoc-in db [:games guid] connected-players)))

(rf/reg-sub
 :games
 (fn [db _]
   (get db :games)))

(rf/reg-sub
 :game
 :<- [:games]
 (fn [[games] [_ guid]]
   (get games guid)))

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

;; TODO Other handlers for default sente events...

(defonce router
  (sente/start-client-chsk-router!
   (:ch-recv ws/client-chsk)
   event-msg-handler))
