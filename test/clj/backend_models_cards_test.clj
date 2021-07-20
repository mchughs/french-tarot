(ns backend-models-cards-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [backend.models.cards :as sut]
   [cards :as cards.data]
   [players :as players.data]))

(def board-1
 #{{:card cards.data/ace-of-spades
    :play-order 0
    :uid (:id players.data/player-one)
    :position (:position players.data/player-one)}
   {:card cards.data/ten-of-spades
    :play-order 1
    :uid (:id players.data/player-two)
    :position (:position players.data/player-two)}
   {:card cards.data/jack-of-spades
    :play-order 2
    :uid (:id players.data/player-three)
    :position (:position players.data/player-three)}
   {:card cards.data/king-of-spades
    :play-order 3
    :uid (:id players.data/player-four)
    :position (:position players.data/player-four)}})

(def board-2
  #{{:card cards.data/king-of-spades
     :play-order 0
     :uid (:id players.data/player-one)
     :position (:position players.data/player-one)}
    {:card cards.data/ten-of-spades
     :play-order 1
     :uid (:id players.data/player-two)
     :position (:position players.data/player-two)}
    {:card cards.data/jack-of-spades
     :play-order 2
     :uid (:id players.data/player-three)
     :position (:position players.data/player-three)}
    {:card  cards.data/ace-of-spades
     :play-order 3
     :uid (:id players.data/player-four)
     :position (:position players.data/player-four)}})

(def board-3
  #{{:card cards.data/king-of-spades
     :play-order 0
     :uid (:id players.data/player-one)
     :position (:position players.data/player-one)}
    {:card cards.data/the-petit
     :play-order 1
     :uid (:id players.data/player-two)
     :position (:position players.data/player-two)}
    {:card cards.data/jack-of-spades
     :play-order 2
     :uid (:id players.data/player-three)
     :position (:position players.data/player-three)}
    {:card  cards.data/ace-of-spades
     :play-order 3
     :uid (:id players.data/player-four)
     :position (:position players.data/player-four)}})

(def board-4
  #{{:card cards.data/king-of-spades
     :play-order 0
     :uid (:id players.data/player-one)
     :position (:position players.data/player-one)}
    {:card cards.data/the-petit
     :play-order 1
     :uid (:id players.data/player-two)
     :position (:position players.data/player-two)}
    {:card cards.data/the-21
     :play-order 2
     :uid (:id players.data/player-three)
     :position (:position players.data/player-three)}
    {:card  cards.data/the-13
     :play-order 3
     :uid (:id players.data/player-four)
     :position (:position players.data/player-four)}})

(def board-5
  #{{:card cards.data/ace-of-spades
     :play-order 0
     :uid (:id players.data/player-one)
     :position (:position players.data/player-one)}
    {:card cards.data/ten-of-spades
     :play-order 1
     :uid (:id players.data/player-two)
     :position (:position players.data/player-two)}
    {:card cards.data/jack-of-spades
     :play-order 2
     :uid (:id players.data/player-three)
     :position (:position players.data/player-three)}
    {:card cards.data/the-excuse
     :play-order 3
     :uid (:id players.data/player-four)
     :position (:position players.data/player-four)}})

(def board-6
  #{{:card cards.data/ace-of-spades
     :play-order 0
     :uid (:id players.data/player-one)
     :position (:position players.data/player-one)}
    {:card cards.data/ten-of-hearts
     :play-order 1
     :uid (:id players.data/player-two)
     :position (:position players.data/player-two)}
    {:card cards.data/jack-of-hearts
     :play-order 2
     :uid (:id players.data/player-three)
     :position (:position players.data/player-three)}
    {:card cards.data/king-of-hearts
     :play-order 3
     :uid (:id players.data/player-four)
     :position (:position players.data/player-four)}})

(deftest find-holder
  (testing "Within the same suit, the king is the strongest."    
    (is {:card cards.data/king-of-spades
         :uid (:id players.data/player-four)
         :position (:position players.data/player-four)}
        (sut/top-card board-1)))
  (testing "Within the same suit, order of play doesn't matter, the king is the strongest."
    (is {:card cards.data/king-of-spades
         :uid (:id players.data/player-one)
         :position (:position players.data/player-one)}
        (sut/top-card board-2)))
  (testing "trump is stronger than all other suit cards."
    (is {:card cards.data/the-petit
         :uid (:id players.data/player-two)
         :position (:position players.data/player-two)}
        (sut/top-card board-3)))
  (testing "the 21 is the strongest trump."
    (is {:card cards.data/the-21
         :uid (:id players.data/player-three)
         :position (:position players.data/player-three)}
        (sut/top-card board-4)))
  (testing "the excuse is never the top card."
    (is {:card cards.data/jack-of-spades
         :uid (:id players.data/player-three)
         :position (:position players.data/player-three)}
        (sut/top-card board-5)))
  (testing "the first suit played holds when only other suits are played thereafter."
    (is {:card cards.data/ace-of-spades
         :uid (:id players.data/player-one)
         :position (:position players.data/player-one)}
        (sut/top-card board-6))))
