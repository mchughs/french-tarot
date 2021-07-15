(ns backend.router
  (:require
   [backend.routes.ws :as ws]
   [backend.models.deck :as deck]
   [clj-uuid :as uuid]
   [clojure.set :as set]
   [clojure.tools.logging :as log]
   [mount.core :refer [defstate]]
   [taoensso.sente :as sente]
   [utils :as utils]))

(defn- broadcast!
  ([event]
   (broadcast! event (:any @(:connected-uids ws/server-chsk))))
  ([event uids]
   (doseq [uid uids]
     ((:send-fn ws/server-chsk) uid event))))

(defmulti event-msg-handler :id)

(defmethod event-msg-handler :default
  [{:keys [_event id _?data _ring-req _?reply-fn _send-fn] :as _payload}]
  (log/infof "Unhandled event: %s" id)
  #_(log/infof "With payload\n%s" (utils/pp-str payload)))

(defmethod event-msg-handler :player-name/fetch
  [_] ;; For illustrative purposes only
  (broadcast! [:player-name/publish {:player-map @ws/*player-map}]))

;; TODO better persistence
(defonce registered-rooms (atom {}))

(defmethod event-msg-handler :room/create
  [{?data :?data}]
  (let [rid (uuid/v4)
        uid (:user-id ?data)]
    (swap! registered-rooms assoc rid {:host uid :players #{uid}})
    (broadcast! [:room/enter {:rid rid}] #{uid})
    (broadcast! [:room/register {:rid rid :user-id uid}])))

(defmethod event-msg-handler :room/join
  [{f :?reply-fn data :?data }]
  (let [{rid :rid user-id :user-id} data
        player-count (count (get @registered-rooms rid))
        other-rooms (dissoc @registered-rooms rid)
        occupied-players (reduce (fn [acc [_ {players :players}]]
                                   (set/union acc players))
                                 #{}
                                 other-rooms)]
    (when f
      (if (and (< 0 player-count 4) ;; room has available spots. 
               (not (contains? occupied-players user-id))) ;; client isn't already part of another existing room.
        (let [updated-rooms (swap! registered-rooms update-in [rid :players] conj user-id) ;; idempotent operation.
              connected-players (get-in updated-rooms [rid :players])]
          (broadcast! [:room/update {:rid rid :connected-players connected-players}])
          (f :chsk/success))
        (f :chsk/error)))))

(defmethod event-msg-handler :room/leave
  [{f :?reply-fn data :?data}]
  (let [{host? :host?
         user-id :user-id
         rid :rid} data
        {players :players :as room} (get @registered-rooms rid)]
    (when f ;; TODO this section illustrates that the dual copies of room state in frontend and backend is getting hard to synchronize.
      (if (and room (contains? players user-id)) ;; The room exists and the user is in it. 
        (let [leaving-players (if host?
                                players  ;; kicks everyone out of the room if the host leaves.
                                #{user-id}) ;; if not the host, then just the requesting user leaves.
              remaining-players (set/difference players leaving-players)]
          (if (empty? remaining-players)
            (swap! registered-rooms dissoc rid)  ;; drop the room from the registry if there are no more players.
            (swap! registered-rooms update-in [rid :players] disj user-id))  ;; drop the player from room
          (broadcast! [:room/exit {}] leaving-players)
          (broadcast! [:room/update {:rid rid :connected-players remaining-players}])
          (f :chsk/success))
        (f :chsk/error)))))

(defmethod event-msg-handler :game/start
  [{f :?reply-fn data :?data uid :uid}]
  (let [{rid :rid} data
        {players :players
         host :host
         closed? :closed?
         :as room} (get @registered-rooms rid)
        host? (= uid host)]
    (when f 
      (if (and room host? (= 4 (count players)) (not closed?)) ;; The room exists, is full, the game request was initiated by the host, and the game isn't already started. 
        (do
          (swap! registered-rooms update rid assoc :closed? true)
          (broadcast! [:room/close {:rid rid}])
          (f :chsk/success))
        (f :chsk/error)))))

(defn- init-player [idx uid]
  {:id uid
   :name (get @ws/*player-map uid)
   :position idx
   :score 0
   :hand #{}})

(defmethod event-msg-handler :round/start
  [{f :?reply-fn data :?data uid :uid}]
  (let [{rid :rid} data
        {players-ids :players
         host :host
         closed? :closed?
         round-history :round-history
         :as room} (get @registered-rooms rid)
        host? (= uid host)]
    (when f      
      (if (and room host? (= 4 (count players-ids)) closed?)
        (if (empty? round-history) ;; first round of the game
          (let [{:keys [players _dog] :as first-round} (as-> players-ids $
                                                          (map-indexed init-player $)
                                                          (deck/deal $
                                                                     (:id (first $))
                                                                     (deck/shuffled-deck)))]
            (swap! registered-rooms update rid assoc :round-history [first-round])
            (doseq [player players
                    :let [event [:round/deal player]
                          uid (:id player)]]
              (broadcast! event [uid])) ;; broadcast! expects a coll of uids
            (f :chsk/success))
          (let [round-history (get-in @registered-rooms [rid :round-history])
                round-number (count round-history)
                {:keys [players dog defenders taker] :as latest-round} (last round-history)
                deck (if (and defenders taker) ;; TODO do an integrity check on the deck so that each card is unique and there are 78
                       (concat (:pile defenders) (:pile taker)) ;; the last round was played out
                       (->> players ;; everyone passed on the last round
                            (mapcat :hand)
                            (concat dog)
                            vec))
                dealer-idx (mod round-number 4) ;; gives us who's turn is is to deal
                dealer-id (->> players
                               (utils/find-first #(= dealer-idx (:position %)))
                               :id)
                next-round (deck/deal players dealer-id deck)]
            (swap! registered-rooms update-in [rid :round-history] conj next-round)
            (doseq [player (:players next-round)
                    :let [event [:round/deal player]
                          uid (:id player)]]
              (broadcast! event [uid]))
            (f :chsk/success)))
        (f :chsk/error)))))

(defmethod event-msg-handler :round/end
  [{f :?reply-fn data :?data uid :uid}]
  (let [{rid :rid} data
        {players-ids :players
         host :host
         closed? :closed?
         round-history :round-history
         :as room} (get @registered-rooms rid)
        host? (= uid host)]
    (when f
      (if (and room host? (= 4 (count players-ids)) closed?)
        (f :chsk/success) ;; TODO
        #_(if (empty? round-history) ;; first round of the game
          (let [{:keys [players _dog] :as first-round} (as-> players-ids $
                                                         (map-indexed init-player $)
                                                         (deck/deal $
                                                                    (:id (first $))
                                                                    (deck/shuffled-deck)))]
            (swap! registered-rooms update rid assoc :round-history [first-round])
            (doseq [player players
                    :let [event [:round/deal player]
                          uid (:id player)]]
              (broadcast! event [uid])) ;; broadcast! expects a coll of uids
            (f :chsk/success))
          (let [round-history (get-in @registered-rooms [rid :round-history])
                round-number (count round-history)
                {:keys [players dog defenders taker] :as latest-round} (last round-history)
                deck (if (and defenders taker) ;; TODO do an integrity check on the deck so that each card is unique and there are 78
                       (concat (:pile defenders) (:pile taker)) ;; the last round was played out
                       (->> players ;; everyone passed on the last round
                            (mapcat :hand)
                            (concat dog)))
                dealer-idx (mod round-number 4) ;; gives us who's turn is is to deal
                dealer-id (->> players
                               (utils/find-first #(= dealer-idx (:position %)))
                               :id)
                next-round (deck/deal players dealer-id deck)]
            (swap! registered-rooms update-in [rid :round-history] conj next-round)
            (doseq [player (:players next-round)
                    :let [event [:round/deal player]
                          uid (:id player)]]
              (broadcast! event [uid]))
            (f :chsk/success)))
        (f :chsk/error)))))

(defmethod event-msg-handler :room/get-ids
  [{:keys [?reply-fn]}]
  (when ?reply-fn
    (?reply-fn {:rooms @registered-rooms})))

;; TODO Other handlers for default sente events...

(defmethod event-msg-handler :chsk/uidport-open
  [{?data :?data}]
  (log/info "Server " (pr-str ?data)))

(defstate router
  :start (sente/start-server-chsk-router!
          (:ch-recv ws/server-chsk)
          event-msg-handler))

(comment
  (reset! registered-rooms {}))
