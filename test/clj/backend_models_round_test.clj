(ns backend-models-round-test
  (:require [clojure.test :refer [deftest is]]
            [malli.core :as m]
            [players :as player.data]
            [backend.models.round :as sut]
            [specs.player :as s.player]
            [specs.round :as s.round]))

;; falls 39 points below the mark
(def sample-pile [{:type :pip, :value 6, :points 0.5, :suit :clubs} {:type :pip, :value 10, :points 0.5, :suit :diamonds} {:type :trump, :value 29, :points 0.5, :ouder? false} {:type :face, :name :queen, :value 13, :points 3.5, :suit :diamonds} {:type :trump, :value 28, :points 0.5, :ouder? false} {:type :pip, :value 3, :points 0.5, :suit :hearts} {:type :pip, :value 4, :points 0.5, :suit :spades} {:type :pip, :value 7, :points 0.5, :suit :diamonds} {:type :trump, :value 15, :points 4.5, :ouder? true} {:type :pip, :value 3, :points 0.5, :suit :diamonds}])

(def p1-bonuses
  {:bonus/won-petit-au-bout 10})

(def p1-bid
  {:type :bid/garde
   :multiplier 2})

(deftest calc-test
  (let [{:keys [taker defenders]}
        (sut/calculate-scores {:player player.data/player-one
                               :pile sample-pile
                               :bid p1-bid
                               :bonuses p1-bonuses}
          #{player.data/player-two
            player.data/player-three
            player.data/player-four})]
    (is (m/validate s.player/Player taker))
    (is (m/validate s.round/Bid p1-bid))
    (is (m/validate s.round/Bonuses p1-bonuses))
    (is (every? #(m/validate s.player/Player %) defenders))
    (is (= -294 (:score taker)))
    (is (every? #(= 98 (:score %)) defenders))))
