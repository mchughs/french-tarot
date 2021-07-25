(ns backend.models.deck
  (:require
   [backend.db :as db]
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [utils :as utils]))

(defonce virgin-deck (edn/read-string (slurp (io/resource "data/deck.edn"))))
(defn shuffled-deck [] (shuffle virgin-deck))

(def player-count 4)
(def dog-card-locations #{6 19 32 45 58 71})

(defn cut-deck [deck]
  (let [cut-point (rand-int 78)]
    (into [] (concat (subvec deck cut-point 78)
                     (subvec deck 0 cut-point)))))

(defn q-deck [round-id]
  (:round/deck
   (db/q1
    '{:find (pull e [*])
      :in [round-id]
      :where [[e :round/id round-id]]}
    round-id)))

(defn q-players [gid]
  (db/q '{:find [player-id position]
          :in [gid]
          :where [[player-id :player/game gid]
                  [player-id :player/user-id players]
                  [player-id :player/position position]]}
        gid))

(defn- divide-cards
  "Divides out cards between each player and the dog in a manner similar to the customary physical dealing process."
  [deck]
  (let [{dog-cards true
         player-cards false} (->> deck
                                  cut-deck
                                  (map-indexed (fn [idx card] [idx card]))
                                  (group-by (fn [[idx _card]]
                                              (contains? dog-card-locations idx)))
                                  (utils/fmap #(map last %)))
        hands (->> player-cards
                   (partition 3)
                   (map-indexed (fn [idx batch] [idx batch]))
                   (group-by (fn [[idx _batch]]
                               (mod idx player-count)))
                   (utils/fmap #(->> %
                                     (map last)
                                     (apply concat)
                                     set)))]
    {:hands hands
     :dog (set dog-cards)}))

(defn deal!
  "Divides out cards between each player and the dog in a manner similar to the customary physical dealing process."
  [gid round-id]
  (let [deck (q-deck round-id)
        players (q-players gid)         
        {:keys [hands dog]} (divide-cards deck)]
    (doseq [[player-id position] players]
      (db/run-fx! ::deal player-id (get hands position)))
    (db/run-fx! ::set-dog round-id dog)))
