(ns backend.models.round)

(def point-threshold
  {0 56
   1 51
   2 41
   3 36})

(defn calculate-scores [taker defenders bonuses]
  (let [{:keys [point-cnt oulders-cnt]}
        (reduce
          (fn [acc {:keys [points ouder?]}]
            (let [ouder-update-fn (if ouder? inc identity)]
              (-> acc
                (update :point-cnt + points)
                (update :oulders-cnt ouder-update-fn))))
          {:point-cnt 0
           :oulders-cnt 0}
          (:pile taker))

        delta (- point-cnt (point-threshold oulders-cnt))
        hand-score (int (* (get-in taker [:bid :multiplier])
                           (+ (Math/abs delta) 25)))
        taker-fn (if (pos? delta) + -)
        defenders-fn (if (pos? delta) - +)]
    {:taker (update (:player taker) :score taker-fn (* (count defenders) hand-score))
     :defenders (map #(update % :score defenders-fn hand-score) defenders)}))
