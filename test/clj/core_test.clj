(ns core-test
  (:require [clojure.test :refer [deftest is]]
            [malli.core :as m]
            [specs.cards :as s.cards]
            [specs.player :as s.player]
            [specs.round :as s.round]
            [backend.models.deck :refer [virgin-deck
                                         shuffled-deck
                                         cut-deck]]))

(def ace-of-spades
  {:type :pip
   :value 1
   :points 0.5
   :suit :spades})

(def king-of-hearts
  {:type :face
   :name :king
   :value 14
   :points 4.5
   :suit :hearts})

(def fake-card
  {:type :face
   :name :super-king
   :value 15 ;; highest possible value is 14
   :points 4.5
   :suit :spades})

(def player-one
  {:id (java.util.UUID/randomUUID)
   :name "Mario"
   :position 0
   :score 0
   :hand #{}})

(def confident-bid
  {:type :bid/garde-contre
   :multiplier 6})

(deftest validation-test
  (is (m/validate s.cards/Card ace-of-spades))
  (is (m/validate s.cards/Card king-of-hearts))
  (is (not (m/validate s.cards/Card fake-card)))
  (is (m/validate s.cards/Deck virgin-deck))
  (is (m/validate s.player/Player player-one))
  (is (m/validate s.cards/Deck (cut-deck virgin-deck)))
  (is (m/validate s.round/Bid confident-bid)))

(deftest shuffle-test
  (is (= 78 (count (shuffled-deck))))
  (is (not= s.cards/Deck (shuffled-deck))))
