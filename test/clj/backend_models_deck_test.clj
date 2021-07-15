(ns backend-models-deck-test
  (:require [clojure.test :refer [deftest is testing]]
            [malli.core :as m]
            [backend.models.deck :as sut]
            [players :as player.data]
            [specs.cards :as s.cards]
            [specs.player :as s.player]))

(deftest deal
  (testing "dealing should divide the cards up"
    (let [{:keys [dog players]}
          (sut/deal #{player.data/player-one
                      player.data/player-two
                      player.data/player-three
                      player.data/player-four}
                    #uuid "e7f42d75-23c6-4c55-b4de-95bb3c8e65e7"
                    (sut/shuffled-deck))
          player-hands (map :hand players)]
      (is (m/validate s.cards/Dog dog))
      (is (every? #(m/validate s.player/Player %) players))
      (is (every? #(m/validate s.cards/Hand %) player-hands)))))
