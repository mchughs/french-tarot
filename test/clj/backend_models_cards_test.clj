(ns backend-models-cards-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [backend.models.cards :as sut]
   [cards :as cards.data]
   [players :as players.data]))

(def board-1
 #{{:board/card cards.data/ace-of-spades
    :board/play-order 0
    :board/uid (:id players.data/player-one)
    :board/position (:position players.data/player-one)}
   {:board/card cards.data/ten-of-spades
    :board/play-order 1
    :board/uid (:id players.data/player-two)
    :board/position (:position players.data/player-two)}
   {:board/card cards.data/jack-of-spades
    :board/play-order 2
    :board/uid (:id players.data/player-three)
    :board/position (:position players.data/player-three)}
   {:board/card cards.data/king-of-spades
    :board/play-order 3
    :board/uid (:id players.data/player-four)
    :board/position (:position players.data/player-four)}})

(def board-2
  #{{:board/card cards.data/king-of-spades
     :board/play-order 0
     :board/uid (:id players.data/player-one)
     :board/position (:position players.data/player-one)}
    {:board/card cards.data/ten-of-spades
     :board/play-order 1
     :board/uid (:id players.data/player-two)
     :board/position (:position players.data/player-two)}
    {:board/card cards.data/jack-of-spades
     :board/play-order 2
     :board/uid (:id players.data/player-three)
     :board/position (:position players.data/player-three)}
    {:board/card  cards.data/ace-of-spades
     :board/play-order 3
     :board/uid (:id players.data/player-four)
     :board/position (:position players.data/player-four)}})

(def board-3
  #{{:board/card cards.data/king-of-spades
     :board/play-order 0
     :board/uid (:id players.data/player-one)
     :board/position (:position players.data/player-one)}
    {:board/card cards.data/the-petit
     :board/play-order 1
     :board/uid (:id players.data/player-two)
     :board/position (:position players.data/player-two)}
    {:board/card cards.data/jack-of-spades
     :board/play-order 2
     :board/uid (:id players.data/player-three)
     :board/position (:position players.data/player-three)}
    {:board/card  cards.data/ace-of-spades
     :board/play-order 3
     :board/uid (:id players.data/player-four)
     :board/position (:position players.data/player-four)}})

(def board-4
  #{{:board/card cards.data/king-of-spades
     :board/play-order 0
     :board/uid (:id players.data/player-one)
     :board/position (:position players.data/player-one)}
    {:board/card cards.data/the-petit
     :board/play-order 1
     :board/uid (:id players.data/player-two)
     :board/position (:position players.data/player-two)}
    {:board/card cards.data/the-21
     :board/play-order 2
     :board/uid (:id players.data/player-three)
     :board/position (:position players.data/player-three)}
    {:board/card  cards.data/the-13
     :board/play-order 3
     :board/uid (:id players.data/player-four)
     :board/position (:position players.data/player-four)}})

(def board-5
  #{{:board/card cards.data/ace-of-spades
     :board/play-order 0
     :board/uid (:id players.data/player-one)
     :board/position (:position players.data/player-one)}
    {:board/card cards.data/ten-of-spades
     :board/play-order 1
     :board/uid (:id players.data/player-two)
     :board/position (:position players.data/player-two)}
    {:board/card cards.data/jack-of-spades
     :board/play-order 2
     :board/uid (:id players.data/player-three)
     :board/position (:position players.data/player-three)}
    {:board/card cards.data/the-excuse
     :board/play-order 3
     :board/uid (:id players.data/player-four)
     :board/position (:position players.data/player-four)}})

(def board-6
  #{{:board/card cards.data/ace-of-spades
     :board/play-order 0
     :board/uid (:id players.data/player-one)
     :board/position (:position players.data/player-one)}
    {:board/card cards.data/ten-of-hearts
     :board/play-order 1
     :board/uid (:id players.data/player-two)
     :board/position (:position players.data/player-two)}
    {:board/card cards.data/jack-of-hearts
     :board/play-order 2
     :board/uid (:id players.data/player-three)
     :board/position (:position players.data/player-three)}
    {:board/card cards.data/king-of-hearts
     :board/play-order 3
     :board/uid (:id players.data/player-four)
     :board/position (:position players.data/player-four)}})

(deftest find-holder
  (testing "Within the same suit, the king is the strongest."    
    (is {:board/card cards.data/king-of-spades
         :board/uid (:id players.data/player-four)
         :board/position (:position players.data/player-four)
         :board/play-order (:position players.data/player-four)}
        (sut/top-card board-1)))
  (testing "Within the same suit, order of play doesn't matter, the king is the strongest."
    (is {:board/card cards.data/king-of-spades
         :board/uid (:id players.data/player-one)
         :board/position (:position players.data/player-one)
         :board/play-order (:position players.data/player-one)}
        (sut/top-card board-2)))
  (testing "trump is stronger than all other suit cards."
    (is {:board/card cards.data/the-petit
         :board/uid (:id players.data/player-two)
         :board/position (:position players.data/player-two)
         :board/play-order (:position players.data/player-two)}
        (sut/top-card board-3)))
  (testing "the 21 is the strongest trump."
    (is {:board/card cards.data/the-21
         :board/uid (:id players.data/player-three)
         :board/position (:position players.data/player-three)
         :board/play-order (:position players.data/player-three)}
        (sut/top-card board-4)))
  (testing "the excuse is never the top card."
    (is {:board/card cards.data/jack-of-spades
         :board/uid (:id players.data/player-three)
         :board/position (:position players.data/player-three)
         :board/play-order (:position players.data/player-three)}
        (sut/top-card board-5)))
  (testing "the first suit played holds when only other suits are played thereafter."
    (is {:board/card cards.data/ace-of-spades
         :board/uid (:id players.data/player-one)
         :board/position (:position players.data/player-one)
         :board/play-order (:position players.data/player-one)}
        (sut/top-card board-6))))
