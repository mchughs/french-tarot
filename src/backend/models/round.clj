(ns backend.models.round
  (:require
   [backend.models.deck :as deck]
   [backend.routes.ws :as ws]
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
        first-round (deck/deal players
                               (:id (first players))
                               (deck/shuffled-deck))]
    [first-round]))

(defn add-next
  "Builds a new round based on the previous round.
   WARNING `round-history` needs to be a vector for conj to work properly."
  [round-history]
  (let [round-number (count round-history)
        {:keys [players dog defenders taker]} (last round-history)
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
        next-round (deck/deal players dealer-id deck)]
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
