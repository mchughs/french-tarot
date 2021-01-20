(ns backend-models-round-test
  (:require [clojure.test :refer :all]
            [malli.core :as m]
            [players :as player.data]
            [backend.models.round :as sut]
            [specs.player :as s.player]))

;; falls 39 points below the mark
(def sample-pile [{:type :pip, :value 6, :points 0.5, :suit :clubs} {:type :pip, :value 10, :points 0.5, :suit :diamonds} {:type :trump, :value 29, :points 0.5, :ouder? false} {:type :face, :name :queen, :value 13, :points 3.5, :suit :diamonds} {:type :trump, :value 28, :points 0.5, :ouder? false} {:type :pip, :value 3, :points 0.5, :suit :hearts} {:type :pip, :value 4, :points 0.5, :suit :spades} {:type :pip, :value 7, :points 0.5, :suit :diamonds} {:type :trump, :value 15, :points 4.5, :ouder? true} {:type :pip, :value 3, :points 0.5, :suit :diamonds}])

(deftest calc-test
  (let [{:keys [taker defenders]}
        (sut/calculate-scores {:player player.data/player-one
                               :pile sample-pile
                               :bid {:type :garde
                                     :multiplier 2}}
          #{player.data/player-two
            player.data/player-three
            player.data/player-four}
          {})]
    (is (m/validate s.player/Player taker))
    (is (every? #(m/validate s.player/Player %) defenders))
    (is (= -354 (:score taker)))
    (is (every? #(= 118 (:score %)) defenders))))
