(ns backend-models-deck-test
  (:require [clojure.test :refer [deftest is testing]]
            [malli.core :as m]
            [backend.models.deck :as sut]
            [specs.cards :as s.cards]))

(deftest divide-cards
  (testing "dealing should divide the cards up"
    (let [{:keys [hands dog]} (sut/divide-cards (sut/shuffled-deck))
          player-hands (map last hands)]
      (is (m/validate s.cards/Dog dog))
      (is (every? #(m/validate s.cards/Hand %) player-hands)))))
