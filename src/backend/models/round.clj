(ns backend.models.round
  (:require [utils :as utils]))

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
        (utils/split-map-by-keys bonuses [:bonus/won-petite-au-bout
                                          :bonus/lost-petite-au-bout])
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
