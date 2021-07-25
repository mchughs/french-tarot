(ns backend.models.game
  (:require
   [backend.db :as db]
   [clj-uuid :as uuid]))
   
(defn create! [rid]
  (let [game-id (uuid/v4)]
    (db/insert!
     game-id
     {:game/id game-id
      :game/status :pre
      :game/room rid
      :game/rounds []})
    game-id))

(defn can-start?
  "The game can start when...
   1. the room exists,
   2. the room is full,
   3. the request to start is coming from the host,
   4. the game doesn't already exist."
  [rid uid]
  (let [players (db/q '{:find [players]
                        :in [rid]
                        :where [[e :room/id rid]
                                [e :room/players players]]}
                      rid)
        host? (ffirst
               (db/q '{:find [e]
                       :in [rid uid]
                       :where [[e :room/id rid]
                               [e :room/host uid]]}
                     rid
                     uid))
        game-id (ffirst
                 (db/q '{:find [game-id]
                         :in [rid]
                         :where [[e :room/id rid]
                                 [e :room/game game-id]]}
                       rid))]
    (and (db/exists? rid) ;; 1.
         (= 4 (count players)) ;; 2. 
         host? ;; 3.
         (nil? game-id) ;; 4.
         )))

(defn start! [rid gid]
  (db/run-fx! ::start rid gid))

(defn get-game [gid]
  (db/q1 '{:find (pull gid [*])
           :in [gid]
           :where [[gid :crux.db/id]]}
         gid))

(defn begin-round! [gid]
  (db/run-fx! ::begin-round gid))

(defn end-round! [gid]
  (db/run-fx! ::end-round gid))
