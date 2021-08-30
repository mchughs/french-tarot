(ns backend-models-log-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [backend.models.logs :as sut]
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
    {:board/card cards.data/the-excuse
     :board/play-order 3
     :board/uid (:id players.data/player-four)
     :board/position (:position players.data/player-four)}})

(deftest return-pile
  (testing "When there is no excuse, the pile is returned as expected regardless of who the taker is."
    (is (= #{cards.data/ace-of-spades
             cards.data/ten-of-spades
             cards.data/jack-of-spades
             cards.data/king-of-spades}
           (sut/get-pile
            board-1
            (:id players.data/player-four)
            (:id players.data/player-four))))
    (is (= #{cards.data/ace-of-spades
             cards.data/ten-of-spades
             cards.data/jack-of-spades
             cards.data/king-of-spades}
           (sut/get-pile
            board-1
            (:id players.data/player-three)
            (:id players.data/player-four)))))
  (testing "When there is the excuse, and the taker played it, the taker owes a card which will be settled at the end of the game."
    (is (= #{cards.data/iou
             cards.data/ten-of-spades
             cards.data/jack-of-spades
             cards.data/king-of-spades}
           (sut/get-pile
            board-2
            (:id players.data/player-four)
            (:id players.data/player-one)))))
  (testing "When there is the excuse, and a defender played it, and the defenders hold, then they keep the pile as usual."
    (is (= #{cards.data/the-excuse
             cards.data/ten-of-spades
             cards.data/jack-of-spades
             cards.data/king-of-spades}
           (sut/get-pile
            board-2
            (:id players.data/player-three)
            (:id players.data/player-one)))))
  (testing "When there is the excuse, and a defender played it, and the taker holds, then the taker takes an IOU"
    (is (= #{cards.data/iou
             cards.data/ten-of-spades
             cards.data/jack-of-spades
             cards.data/king-of-spades}
           (sut/get-pile
            board-2
            (:id players.data/player-one)
            (:id players.data/player-one))))))
