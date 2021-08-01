(ns backend-models-round-test
  (:require [clojure.test :refer [deftest is]]
            [malli.core :as m]
            [players :as player.data]
            [backend.models.round :as sut]
            [specs.round :as s.round]))

;; falls 39 points below the mark
(def sample-pile [{:type :pip, :value 6, :points 0.5, :suit :clubs} {:type :pip, :value 10, :points 0.5, :suit :diamonds} {:type :trump, :value 29, :points 0.5, :ouder? false} {:type :face, :name :queen, :value 13, :points 3.5, :suit :diamonds} {:type :trump, :value 28, :points 0.5, :ouder? false} {:type :pip, :value 3, :points 0.5, :suit :hearts} {:type :pip, :value 4, :points 0.5, :suit :spades} {:type :pip, :value 7, :points 0.5, :suit :diamonds} {:type :trump, :value 15, :points 4.5, :ouder? true} {:type :pip, :value 3, :points 0.5, :suit :diamonds}])

(def p1-bonuses
  {:bonus/won-petit-au-bout 10})

(def p1-bid :bid/garde)

(deftest calc-test
  (let [{t-score :taker/score
         d-score :defenders/score}
        (sut/calculate-scores {:pile sample-pile
                               :bid p1-bid
                               :bonuses p1-bonuses}
                              {:uids #{(:id player.data/player-two)
                                       (:id player.data/player-three)
                                       (:id player.data/player-four)}})]    
    (is (m/validate s.round/Bid p1-bid))
    (is (m/validate s.round/Bonuses p1-bonuses))
    (is (= -324 t-score))
    (is (= 108 d-score))))
