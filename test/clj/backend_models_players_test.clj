(ns backend-models-players-test
  (:require [clojure.test :refer [deftest is testing]]
            [malli.core :as m]
            [players :as player.data]
            [specs.player :as s.player]))

(deftest divide-cards
    (testing "player spec should be followed"
      (let [players #{player.data/player-one
                      player.data/player-two
                      player.data/player-three
                      player.data/player-four}]
        (is (every? #(m/validate s.player/Player %) players)))))
