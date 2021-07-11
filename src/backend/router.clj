(ns backend.router
  (:require
   [backend.routes.ws :as ws]
   [clj-uuid :as uuid]
   [clojure.set :as set]
   [clojure.tools.logging :as log]
   [mount.core :refer [defstate]]
   [taoensso.sente :as sente]))

(defn- broadcast! [event]
  (doseq [uid (:any @(:connected-uids ws/server-chsk))]
    ((:send-fn ws/server-chsk) uid event)))

(defmulti event-msg-handler :id)

(defmethod event-msg-handler :default
  [{:keys [_event id _?data _ring-req _?reply-fn _send-fn] :as payload}]
  (log/infof "Unhandled event: %s" id)
  #_(log/debugf "With payload\n%s" (utils/pp-str payload)))

;; TODO better persistence
(defonce registered-games (atom {}))

(defmethod event-msg-handler :game/create
  [{?data :?data}]
  (let [guid (uuid/v4)
        uid (:user-id ?data)]
    (swap! registered-games assoc guid #{uid})
    (broadcast! [:game/register {:guid guid :user-id uid}])))

(defmethod event-msg-handler :game/join
  [{f :?reply-fn data :?data}]
  (let [{guid :guid user-id :user-id} data
        player-count (count (get @registered-games guid))
        occupied-players (reduce (fn [acc [_ players]]
                                   (set/union acc players))
                                 #{}
                                 @registered-games)]
    (when f
      (if (and (< 0 player-count 4) ;; game has an available spot.
               (not (contains? occupied-players user-id))) ;; client isn't already part of an existing game.
        (let [updated-games (swap! registered-games update guid conj user-id)
              connected-players (get updated-games guid)]
          (broadcast! [:game/update {:guid guid
                                     :connected-players connected-players}])
          (f :chsk/success))
        (f :chsk/error)))))

(defmethod event-msg-handler :game/get-ids
  [{:keys [?reply-fn]}]
  (when ?reply-fn
    (?reply-fn {:games @registered-games})))

;; TODO Other handlers for default sente events...

(defmethod event-msg-handler :chsk/uidport-open
  [{?data :?data}]
  (log/info "Server " (pr-str ?data)))

(defstate router
  :start (sente/start-server-chsk-router!
          (:ch-recv ws/server-chsk)
          event-msg-handler))
