(ns players)

(def player-one
  {:id #uuid "e7f42d75-23c6-4c55-b4de-95bb3c8e65e7"
   :name "Mario"
   :position 0
   :score 30
   :hand #{}})

(def player-two
  {:id (java.util.UUID/randomUUID)
   :name "Luigi"
   :position 1
   :score -10
   :hand #{}})

(def player-three
  {:id (java.util.UUID/randomUUID)
   :name "Peach"
   :position 2
   :score -10
   :hand #{}})

(def player-four
  {:id (java.util.UUID/randomUUID)
   :name "Toad"
   :position 3
   :score -10
   :hand #{}})
