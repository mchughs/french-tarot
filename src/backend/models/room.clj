(ns backend.models.room
  (:require
   [backend.db :as db]
   [backend.models.user :as user]
   [clj-uuid :as uuid]
   [clojure.set :as set]))

(defn create [uid]
  (let [rid (uuid/v4)
        username (user/get-username uid)]
    {:room/id rid
     :room/host uid
     :room/hostname username
     :room/players #{uid}
     :room/playernames {uid username}
     :room/status :open
     :room/game nil}))

(defn get-room
  "Returns the room associated with rid."
  [rid]
  (ffirst
   (db/q '{:find [(pull room [*])]
           :in [rid]
           :where [[room :room/id rid]]}
         rid)))

(defn get-rooms
  "Returns all the rooms as a map."
  []
  (reduce
   (fn [acc [room]]
     (assoc acc (:room/id room) room))
   {}
   (db/q '{:find [(pull room [:room/id
                              :room/host
                              :room/hostname
                              :room/players
                              :room/playernames
                              :room/status])]
           :where [[room :room/id]]})))

(defn can-join?
  "The user can join if
   1. the room exists
   2. the room has available spots
   3. the user is not already part of another existing room"
  [rid joiner-uid]
  (let [status (db/q1 '{:find status
                        :in [rid]
                        :where [[e :room/id rid]
                                [e :room/status status]]}
                      rid)
        room (db/q1 '{:find room
                      :in [uid]
                      :where [[e :user/id uid]
                              [e :user/room room]]}
                    joiner-uid)]
    (and (db/exists? rid) ;; room exists
         (= :open status) ;; room has available spots. 
         (nil? room)))) ;; joiner isn't already in any other rooms.

(defn add-player! [rid uid]
  (db/run-fx! ::add-player rid uid (user/get-username uid)))

(defn can-leave?
  "The user can leave if
   1. the room exists
   2. the user is in it"
  [rid leaver-uid]
  (let [room (db/q1 '{:find room
                      :in [uid]
                      :where [[e :user/id uid]
                              [e :user/room room]]}
                    leaver-uid)]
    (and (db/exists? rid)
         (= room rid))))

(defn remove-player!
  "Kicks all players out of the room if the host leaves.
   Deletes the room if there are no remaining players.
   Returns the leavers."
  [rid uid]
  (let [{host :room/host
         players :room/players} (get-room rid)
        leaving-players (if (= host uid)
                          players  ;; kicks everyone out of the room if the host leaves.
                          #{uid}) ;; if not the host, then just the requesting user leaves.
        remaining-players (set/difference players leaving-players)]
    (if (empty? remaining-players)
      (db/delete! rid) ;; drop the room from the registry if there are no more players.
      (db/run-fx! ::remove-player rid uid))
    leaving-players))
