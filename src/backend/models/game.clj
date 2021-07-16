(ns backend.models.game)

(defn can-start?
  "The game can start when...
   1. the room exists,
   2. the room is full,
   3. the request to start is coming from the host,
   4. the game is in the :pre-game state."
  [{:keys [players host game-status] :as room} uid]
  (and room
       (= 4 (count players))
       (= uid host)
       (= game-status :pre-game)))
