(ns backend.models.logs
  (:require
   [backend.db :as db]
   [backend.models.cards :as cards]
   [backend.models.players :as players]
   [clj-uuid :as uuid]
   [utils :as utils]))

(defn init-log! []
  (let [log-id (uuid/v4)]
    (db/insert! log-id
                {:log/prev-id nil
                 :log/id log-id
                 :log/idx 0
                 :log/phase :bidding
                 :log/player-turn 1
                 :log/highest-bid {}
                 :log/available-bids [:bid/petit
                                      :bid/garde
                                      :bid/garde-sans
                                      :bid/garde-contre]})
    log-id))

(defn get-log
  "Returns the log associated with rid."
  [log-id]  
  (db/q1 '{:find (pull e [*])
           :in [log-id]
           :where [[e :log/id log-id]]}
         log-id))


(defn log-id->round
  "Returns the log associated with rid."
  [log-id]
  (db/q1 '{:find (pull e [*])
           :in [log-id]
           :where [[e :round/logs log-id]]}
         log-id))

(defn place-bid!
  [log bid uid players]
  (let [#:log{:keys [idx
                     highest-bid
                     player-turn
                     available-bids]} log
        passing? (= :pass bid)
        any-prior-takers? (seq highest-bid)
        final-bidder? (= 3 idx)
        new-log-data (cond
                  ;; Playing it out. While passing.
                       (and final-bidder? any-prior-takers? passing?)
                       {:log/phase :announcements
                        :log/taker highest-bid
                        :log/defenders {:uids (disj players (:uid highest-bid))}
                        :log/player-turn (mod (inc player-turn) 4)
                        :log/announcements {}}

                  ;; Playing it out. While taking.
                       (or (and final-bidder? any-prior-takers?)
                           (= :bid/garde-contre bid))
                       {:log/phase :announcements
                        :log/taker {:uid uid :bid bid}
                        :log/defenders {:uids (disj players uid)}
                        :log/player-turn (mod (inc player-turn) 4)
                        :log/announcements {}}

                  ;; Everyone passed.
                       (and final-bidder? passing?)
                       {:log/phase :end}

                  ;; User is bidding
                       (not passing?)
                       {:log/phase :bidding
                        :log/player-turn (mod (inc player-turn) 4)
                        :log/highest-bid {:uid uid :bid bid}
                        :log/available-bids (last (split-at (inc (.indexOf available-bids bid)) available-bids))}

                  ;; User is passing.
                       :else
                       {:log/phase :bidding
                        :log/player-turn (mod (inc player-turn) 4)
                        :log/highest-bid highest-bid
                        :log/available-bids available-bids})]
    new-log-data))

(defn add-log! [new-log-data old-log]
  (let [new-id (uuid/v4)
        new-log (merge new-log-data
                       {:log/prev-id (:log/id old-log)
                        :log/id new-id
                        :log/idx (inc (:log/idx old-log))})]
    (db/insert! new-id new-log)
    new-id))

(defn make-dog [log dog]
  (let [last-player? (= 4 (count (:log/announcements log)))]
    (if-not last-player?
      log
      (case (get-in log [:log/taker :bid])
        :bid/petit
        (-> log
            (assoc :log/phase :dog-construction)
            (assoc :log/dog dog))
        :bid/garde
        (-> log
            (assoc :log/phase :dog-construction)
            (assoc :log/dog dog))
        :bid/garde-sans
        (-> log
            (assoc :log/phase :main)
            (assoc-in [:log/taker :pile] dog))
        :bid/garde-contre
        (-> log
            (assoc :log/phase :main)
            (assoc-in [:log/defenders :pile] dog))))))

(defn make-announcement
  [log announcement uid]
  (update log :log/announcements assoc uid announcement))

(defn init-dog
  [old-log init-taker-pile]
  (let [dealer-turn (db/q1 '{:find turn
                             :in [log-id]
                             :where [[e :round/logs log-id]
                                     [e :round/dealer-turn turn]]}
                           (:log/id old-log))
        new-log (-> old-log
                    (assoc-in [:log/taker :pile] init-taker-pile)
                    (assoc :log/phase :main)
                    (dissoc :log/dog)
                    (assoc :log/player-turn (mod (inc dealer-turn) 4))
                    (assoc :log/board []))]
    new-log))

(defn user-turn?
  [log uid]
  (let [turn (:log/player-turn log)
        turn-uid (db/q1 '{:find uid
                          :in [log-id turn]
                          :where [[round :round/logs log-id]
                                  [game :game/rounds round]
                                  [player-id :player/game game-id]
                                  [player-id :player/user-id uid]
                                  [player-id :player/position turn]]}
                        (:log/id log)
                        turn)]    
    (= turn-uid uid)))

(defn allowed-card?
  "Lets the player know if they are allowed to play their card given the context."
  [log card uid]
  (let [board (:log/board log)
        hand (db/q1 '{:find (distinct hand)
                      :in [log-id uid]
                      :where [[round :round/logs log-id]
                              [game :game/rounds round]
                              [player-id :player/game game-id]
                              [player-id :player/hand hand]
                              [player-id :player/user-id uid]]}
                    (:log/id log)
                    uid)
        first-card (:board/card (first board))
        obliged-suit (if (= :excuse (:type first-card))
                       (:suit (:board/card (second board)))
                       (:suit first-card))]    
    (or (= :excuse (:type card)) ;; Excuse can be played at anytime
        (empty? board) ;; Opening card can be anything. ;; TODO on first turn there are some choices which are not allowed
        (and (= :excuse (:type first-card)) ;; If the first card played is the excuse, the next card can be anything.
             (= 1 (count board)))
        (= obliged-suit (:suit card)) ;; If the card matches the suit of the opening card then it's okay
        (and (= :trump (:type card))
             (empty? (filter #(= obliged-suit (:suit %)) hand))) ;; If the card doesn't match the suit but the player doesn't have a card of the same, then it's okay.
        (and (empty? (filter #(= :trump (:type %)) hand)) ;; If the player doesn't have a matching suit or any trump cards then they can play anything.
             (empty? (filter #(= obliged-suit (:suit %)) hand))))))

(defn play-card ;; TODO Excuse
  [log card uid]
  (let [board (:log/board log)
        player (players/get-player uid)
        new-board (conj board {:board/play-order (count board)
                               :board/uid uid
                               :board/position (:player/position player)
                               :board/card card})]
    (-> log
        (assoc :log/board new-board)
        (update :log/player-turn #(mod (inc %) 4)))))

(defn- insert-iou [cards]
  (-> cards
      (disj {:type :excuse
             :points 4.5
             :ouder? true})
      ;; TODO technically with a schlem there could not be any white cards to donate.
      (conj {:type :iou
             :points 0.5
             :ouder? false})))

(defn get-pile
  "The pile would ordinarily just be a set of all the cards on the board, however the excuse complicates things.
   Helps to notice that whoever plays the excuse cannot be the holder."
  [board taker-id holder-id]
  (let [cards (set (map :board/card board))
        excuse-player (->> board
                           (utils/find-first #(= :excuse (:type (:board/card %))))
                           :board/uid)]    
    (cond
      ;; no excuse played
      (nil? excuse-player)
      cards

      ;; taker plays excuse.
      (= taker-id excuse-player)
      (insert-iou cards)
      
      ;; defender plays excuse and taker holds
      (and (not= taker-id excuse-player)
           (= taker-id holder-id))
      (insert-iou cards)

      :else
      cards)))

(defn make-trick
  [log uid]
  (let [board (:log/board log)
        player (players/get-player uid)
        holder (cards/top-card board)
        taker-id (get-in log [:log/taker :uid])
        holder-id (:board/uid holder)
        holder-team (if (= taker-id holder-id)
                      :log/taker
                      :log/defenders)
        pile (get-pile board taker-id holder-id)]
    (-> log
        (assoc :log/board [])
        (update-in [holder-team :pile] concat pile)
        (assoc :log/player-turn (:board/position holder))
        (assoc :log/phase (if (zero? (count (:player/hand player))) ;; last trick
                            :scoring
                            :main)))))
