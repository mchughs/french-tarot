(ns backend.models.round
  (:require
   [backend.db :as db]
   [backend.models.deck :as deck]
   [clj-uuid :as uuid]
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

(def ^:private multipliers
  {:bid/petit 1
   :bid/garde 2
   :bid/garde-sans 4
   :bid/garde-contre 6})

(defn calculate-scores
  [{:keys [pile bonuses bid] :as _taker} defenders]
  (let [delta (calculate-delta pile)
        taker-fn (if (pos? delta) + -)

        [pre-mult-bonuses
         post-mult-bonuses]
        (utils/split-map-by-keys bonuses
                                 [:bonus/won-petit-au-bout
                                  :bonus/lost-petit-au-bout])
        taker-hand-score
        (int
         (taker-fn
          (+ (reduce + 0 post-mult-bonuses)
             (* (multipliers bid)
                (+ (Math/abs delta)
                   (taker-fn (reduce + 0 (vals pre-mult-bonuses)))
                   25)))))]
    {:taker/score (* (count (:uids defenders)) taker-hand-score)
     :defenders/score (* -1 taker-hand-score)}))

(defn- init-round! [log-id]
  (let [round-id (uuid/v4)]
    (db/insert! round-id
                {:round/prev-id nil
                 :round/id round-id
                 :round/logs [log-id]
                 :round/dealer-turn 0
                 :round/deck (deck/shuffled-deck)})
    round-id))

(defn start! [gid log-id]
  (let [round-id (init-round! log-id)]
    (db/run-fx! ::start gid round-id)
    round-id))

(defn append-log! [old-log-id new-log-id]
  (let [round-id (db/q1 '{:find round-id
                          :in [log-id]
                          :where [[e :round/logs log-id]
                                  [e :round/id round-id]]}
                        old-log-id)]
    (db/run-fx! ::append-log round-id new-log-id)
    round-id))

(defn get-round [round-id]
  (db/q1 '{:find (pull round-id [*])
           :in [round-id]
           :where [[round-id :crux.db/id]]}
         round-id))

(defn can-start?
  "The round can start when...
   1. the room exists,
   2. the room is closed,
   3. the request to start is coming from the host,
   4. the game is in the :pre state,
   5. TODO all players have marked they are ready." ;; TODO
  [rid uid]
  (let [full? (db/q1 '{:find e
                       :in [rid]
                       :where [[e :room/id rid]
                               [e :room/status :closed]]}
                     rid)
        host? (db/q1 '{:find e
                       :in [rid uid]
                       :where [[e :room/id rid]
                               [e :room/host uid]]}
                     rid
                     uid)
        in-progress? (db/q1 '{:find e
                              :in [rid]
                              :where [[e :game/room rid]
                                      [e :game/status :pre]]}
                            rid)]
    (and (db/exists? rid) ;; 1.
         full? ;; 2. 
         host? ;; 3.
         in-progress? ;; 4.
         )))

(defn score [log]
  (let [{taker-score :taker/score
         defenders-score :defenders/score} (calculate-scores (:log/taker log)
                                                             (:log/defenders log))]
    (-> log
        (assoc :log/phase :end)
        (assoc-in [:log/taker :score] taker-score)
        (assoc-in [:log/defenders :score] defenders-score))))

(defn next-dealer! [round-id]
  (db/run-fx! ::next-dealer round-id))
