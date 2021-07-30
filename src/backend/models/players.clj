(ns backend.models.players
  (:require   
   [backend.db :as db]
   [clj-uuid :as uuid]))

(defn create!
  "Returns the uids for each player."
  [rid gid]
  (let [players (db/q '{:find [players name]
                        :in [rid]
                        :where [[e :room/players players]
                                [e :room/id rid]
                                [x :user/id players]
                                [x :user/name name]]}
                      rid)]
    (doseq [[idx [uid username]] (map-indexed vector players)]
      (let [player-id (uuid/v4)]
        (db/insert!
         player-id
         {:player/id player-id
          :player/game gid
          :player/user-id uid
          :player/name username
          :player/position idx
          :player/score 0
          :player/hand #{}})))
    (set (map first players))))

(defn get-players
  "WARNING: Returns user-ids, not player-ids!!!"
  [gid]
  (db/q1 '{:find (distinct uid)
           :in [gid]
           :where [[pid :player/game gid]
                   [pid :player/user-id uid]]}
        gid))

(defn get-player
  [uid]
  (db/q1 '{:find (pull e [*])
           :in [uid]
           :where [[e :player/user-id uid]]}
         uid))

(defn get-public-player-data [gid]
  (let [uids (get-players gid)]
    (map (comp #(select-keys % [:player/user-id :player/name :player/position :player/score])
               get-player) uids)))

(defn log->players
  [log-id]
  (db/q1 '{:find (distinct uid)
           :in [log-id]
           :where [[round :round/logs log-id]
                   [game :game/rounds round]
                   [player-id :player/game game-id]
                   [player-id :player/user-id uid]]}
         log-id))

(defn uid->pid
  [uid]
  (db/q1 '{:find pid
           :in [uid]
           :where [[e :player/user-id uid]
                   [e :player/id pid]]}
         uid))

(defn add-dog-to-hand! [player-id dog]  
  (db/run-fx! ::add-dog-to-hand player-id dog))

(defn remove-dog-from-hand! [player-id init-pile]
  (db/run-fx! ::remove-dog-from-hand player-id init-pile))

(defn append-scores! [log uids]  
  (doseq [uid uids
          :let [score (if (= uid (get-in log [:log/taker :uid]))
                        (get-in log [:log/taker :score])
                        (get-in log [:log/defenders :score]))
                pid (uid->pid uid)]]
    (db/run-fx! ::append-scores pid score)))
