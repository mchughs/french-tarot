(ns backend.models.room
  (:require 
   [clj-uuid :as uuid]
   [clojure.set :as set]))

(defn create [uid]
  (let [rid (uuid/v4)]
    {:rid rid :host uid :players #{uid} :game-status :pre-game}))

(defn can-join?
  "The user can join if
   1. the room exists
   2. the room has available spots
   3. the user is not already part of another existing room"
  [rooms rid joiner-uid]
  (if-let [room (get rooms rid)]
    (let [player-count (count (:players room))
          other-rooms (dissoc rooms rid)
          occupied-players (reduce (fn [acc [_ {players :players}]]
                                     (set/union acc players))
                                   #{}
                                   other-rooms)]
      (and (< 0 player-count 4) ;; room has available spots. 
           (not (contains? occupied-players joiner-uid))))
    false))

(defn add-player [room uid]
  (update room :players conj uid))

(defn can-leave?
  "The user can leave if
   1. the room exists
   2. the user is in it"
  [rooms rid leaver-uid]
  (let [{players :players :as room} (get rooms rid)]
    (and room (contains? players leaver-uid))))

(defn remove-player
  "Returns nil if the host leaves the room."
  [{:keys [host players] :as room} uid]
  (let [leaving-players (if (= host uid)
                          players  ;; kicks everyone out of the room if the host leaves.
                          #{uid}) ;; if not the host, then just the requesting user leaves.
        remaining-players (set/difference players leaving-players)]
    (when-not (empty? remaining-players)
      (update room :players disj uid))))
