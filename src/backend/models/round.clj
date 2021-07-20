(ns backend.models.round
  (:require
   [backend.models.cards :as cards]
   [backend.models.deck :as deck]
   [backend.routes.ws :as ws]
   [clojure.set :as set]
   [utils :as utils]))

(def point-threshold
  {0 56
   1 51
   2 41
   3 36})

(defn calculate-delta [pile]
  (let [{:keys [point-cnt oulders-cnt]}
        (reduce
         (fn [acc {:keys [points ouder?]}]
           (let [ouder-update-fn (if ouder? inc identity)]
             (-> acc
                 (update :point-cnt + points)
                 (update :oulders-cnt ouder-update-fn))))
         {:point-cnt 0
          :oulders-cnt 0}
         pile)]
    (- point-cnt
       (point-threshold oulders-cnt))))

(defn calculate-scores
  [{:keys [pile bonuses bid player] :as _taker} defenders]
  (let [delta (calculate-delta pile)
        taker-fn (if (pos? delta) + -)

        [pre-mult-bonuses
         post-mult-bonuses]
        (utils/split-map-by-keys bonuses
                                 [:bonus/won-petit-au-bout
                                  :bonus/lost-petit-au-bout])
        hand-score
        (int
         (taker-fn
          (+ (reduce + 0 post-mult-bonuses)
             (* (get bid :multiplier)
                (+ (Math/abs delta)
                   (taker-fn (reduce + 0 (vals pre-mult-bonuses)))
                   25)))))]

    {:taker (update player :score + (* (count defenders) hand-score))
     :defenders (map #(update % :score - hand-score) defenders)}))

(defn- init-player [*player-map idx uid]
  {:id uid
   :name (get *player-map uid)
   :position idx
   :score 0
   :hand #{}})

(defn init-history
  "Builds the first round."
  [player-ids]
  (let [players (map-indexed (partial init-player @ws/*player-map) player-ids)
        first-round (assoc (deck/deal players
                                      (:id (first players))
                                      (deck/shuffled-deck))
                           :round-log [{:phase :bidding
                                        :dealer-turn 0
                                        :player-turn 1
                                        :highest-bid {}
                                        :available-bids [:bid/petit
                                                         :bid/garde
                                                         :bid/garde-sans
                                                         :bid/garde-contre]}])]
    [first-round]))

(defn add-next
  "Builds a new round based on the previous round.
   WARNING `round-history` needs to be a vector for conj to work properly."
  [round-history]
  (let [round-number (count round-history)
        {:keys [players dog round-log]} (last round-history)
        {:keys [defenders taker]} (last round-log) ;; TODO do an integrity check to see that the last log is phase :over
        deck (if (and defenders taker) ;; TODO do an integrity check on the deck so that each card is unique and there are 78
               (concat (:pile defenders) (:pile taker)) ;; the last round was played out
               (->> players ;; everyone passed on the last round
                    (mapcat :hand)
                    (concat dog)
                    vec))
        dealer-idx (mod round-number 4) ;; gives us who's turn is is to deal
        dealer-id (->> players
                       (utils/find-first #(= dealer-idx (:position %)))
                       :id)
        next-round (assoc (deck/deal players dealer-id deck)
                          :round-log [{:phase :bidding
                                       :dealer-turn dealer-idx
                                       :player-turn (mod (inc dealer-idx) 4)
                                       :highest-bid {}
                                       :available-bids [:bid/petit
                                                        :bid/garde
                                                        :bid/garde-sans
                                                        :bid/garde-contre]}])]
    (conj round-history next-round)))

(defn can-start?
  "The round can start when...
   1. the room exists,
   2. the room is full,
   3. the request to start is coming from the host,
   4. the game is in the :in-progress state,
   5. TODO all players have marked they are ready." ;; TODO
  [{:keys [players host game-status] :as room} uid]
  (and room
       (= 4 (count players))
       (= uid host)
       (= game-status :in-progress)))

(defn place-bid
  [round-history bid uid]
  (let [logs (:round-log (last round-history))
        {:keys [highest-bid
                dealer-turn
                player-turn
                available-bids]} (last logs)
        passing? (= :pass bid)
        any-prior-takers? (not (empty? highest-bid))
        final-bidder? (= (count logs) 4)
        new-log (cond
                  ;; Playing it out.
                  (and final-bidder? any-prior-takers?)
                  (let [players (:players (last round-history))
                        taker-player (utils/find-first
                                      #(= (if passing?
                                            (:player-id highest-bid)
                                            uid)
                                          (:id %))
                                      players)]
                    {:phase :announcements
                     :taker {:player (dissoc taker-player :hand) ;; TODO, shouldn't be public. should probably be encrypted and then maybe decrypted later.
                             :bid (if passing? (:bid highest-bid) bid)}
                     :defenders {:members (->> players
                                               (remove #(= (:id taker-player) (:id %)))
                                               (mapv #(dissoc % :hand)))}
                     :dealer-turn dealer-turn
                     :player-turn (mod (inc player-turn) 4)})

                  ;; Everyone passed.
                  (and final-bidder? passing?)
                  {:phase :end
                   :dealer-turn dealer-turn}

                  ;; User is bidding
                  (not passing?)
                  {:phase :bidding
                   :dealer-turn dealer-turn
                   :player-turn (mod (inc player-turn) 4)
                   :highest-bid {:player-id uid :bid bid}
                   :available-bids (last (split-at (inc (.indexOf available-bids bid)) available-bids))}

                  ;; User is passing.
                  :else
                  {:phase :bidding
                   :dealer-turn dealer-turn
                   :player-turn (mod (inc player-turn) 4)
                   :highest-bid highest-bid
                   :available-bids available-bids})]
    (conj (pop round-history)
          (assoc (peek round-history)
                 :round-log                 
                 (conj logs new-log)))))

(defn- make-dog [log dog]
  (let [last-player? (= 4 (count (:announcements log)))]
    (if-not last-player?
      log
      (case (get-in log [:taker :bid])
        :bid/petit
        (-> log
            (assoc :phase :dog-construction)
            (assoc :dog dog))
        :bid/garde
        (-> log
            (assoc :phase :dog-construction)
            (assoc :dog dog))
        :bid/garde-sans
        (-> log
            (assoc :phase :main)
            (assoc-in [:taker :pile] dog))
        :bid/garde-contre
        (-> log
            (assoc :phase :main)
            (assoc-in [:defenders :pile] dog))))))

(defn make-announcement
  [round-history announcement uid]
  (let [{logs :round-log dog :dog :as round} (last round-history)
        new-log (-> (last logs)
                    (update :announcements assoc uid announcement)
                    (make-dog dog))]
    (if (= :dog-construction (:phase new-log))
      (let [taker (utils/find-first #(= (get-in new-log [:taker :player :id])
                                        (:id %))
                                    (:players round))
            taker-idx (.indexOf (:players round) taker)]        
        (conj (pop round-history)
              (-> (peek round-history)
                  (assoc :round-log (conj logs new-log))
                  (update :players vec) ;; needs to be vector
                  (update-in [:players taker-idx]
                             (fn [taker]                               
                               (update taker :hand set/union dog))))))
      (conj (pop round-history)
              (-> (peek round-history)
                  (assoc :round-log (conj logs new-log)))))))

(defn init-dog
  [round-history init-taker-pile]
  (let [round (last round-history)
        logs (:round-log round)
        last-log (last logs)
        new-log (-> last-log
                    (assoc-in [:taker :pile] init-taker-pile)
                    (assoc :phase :main))
        taker (utils/find-first #(= (get-in new-log [:taker :player :id])
                                    (:id %))
                                (:players round))
        taker-idx (.indexOf (:players round) taker)]
    (conj (pop round-history)
          (-> (peek round-history)
              (assoc :round-log (conj logs new-log))
              (update :players vec) ;; needs to be vector
              (update-in [:players taker-idx]
                         (fn [taker]
                           (update taker :hand set/difference init-taker-pile)))))))

(defn user-turn?
  [round-history uid]
  (let [last-round (last round-history)
        last-log (last (:round-log last-round))
        turn (:player-turn last-log)
        turn-uid (:id (utils/find-first #(= turn (:position %))
                               (:players last-round)))]
    (= turn-uid uid)))

(defn play-card ;; TODO Excuse
  [round-history card uid]
  (let [last-round (last round-history)
        logs (:round-log last-round)
        {board :board :as last-log} (last logs)
        player (utils/find-first #(= uid (:id %))
                                 (:players last-round))
        new-board (conj board {:play-order (count board)
                               :uid uid
                               :position (:position player)
                               :card card})
        new-log (if (= 4 (count new-board))
                  (let [holder (cards/top-card new-board)
                        holder-team (if (= (get-in last-log [:taker :player :id])
                                           (:uid holder))
                                      :taker
                                      :defenders)]
                    (-> last-log
                        (assoc :board [])
                        (update-in [holder-team :pile] concat (map :card new-board))
                        (assoc :player-turn (:position holder))
                        (assoc :phase (if (= 1 (count (:hand player))) ;; last trick
                                        :scoring
                                        :main))))
                  (-> last-log
                      (assoc :board new-board)
                      (update :player-turn #(mod (inc %) 4))))]
    (conj (pop round-history)
          (-> (peek round-history)
              (assoc :round-log (conj logs new-log))
              (update :players
                      (fn [players]
                        (mapv #(if (= uid (:id %))
                                 (update % :hand disj card)
                                 %)
                              players)))))))

(defn allowed-card?
  "Lets the player know if they are allowed to play their card given the context."
  [round-history uid card]
  (let [last-round (last round-history)
        board (:board (last (:round-log last-round)))
        hand (:hand (utils/find-first #(= uid (:id %))
                                      (:players last-round)))
        first-card (:card (first board))
        obliged-suit (if (= :excuse (:type first-card))
                       (:suit (:card (second board)))
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

(defn score [round-history] ;; TODO gotta fix
  (let [last-round (last round-history)
        logs (:round-log last-round)
        last-log (last logs)
        {:keys [taker defenders]} (calculate-scores (:taker last-log)
                                                    (:defenders last-log))
        new-log (-> last-log
                    (assoc :phase :end)
                    (update :dealer-turn #(mod (inc %) 4))
                    (assoc :taker taker)
                    (assoc :defenders defenders))]
    (conj (pop round-history)
          (-> (peek round-history)
              (assoc :round-log (conj logs new-log))
              (update :players
                      (fn [players]
                        (mapv #(let [uid (:id %)]
                                 (prn "QWER" % taker defenders)
                                 (update % :score + 10))
                              players)))))))
