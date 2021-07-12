(ns backend.models.deck
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [utils :as utils]))

(defonce virgin-deck (edn/read-string (slurp (io/resource "data/deck.edn"))))
(defonce shuffled-deck (shuffle virgin-deck))

(def player-count 4)
(def dog-card-locations #{6 19 32 45 58 71})

(defn cut-deck [deck]
  (let [cut-point (rand-int 78)]
    (into [] (concat (subvec deck cut-point 78)
                     (subvec deck 0 cut-point)))))

(defn deal
  "Divides out cards between each player and the dog in a manner similar to the customary physical dealing process."
  [players dealer-id deck]
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
                                     set)))
        dealer-position (:position (utils/find-first #(= dealer-id (:id %)) players))
        deal-order [(mod (inc dealer-position) player-count)
                    (mod (+ 2 dealer-position) player-count)
                    (mod (+ 3 dealer-position) player-count)
                    dealer-position]]
    {:dog (set dog-cards)
     :players (map-indexed
               (fn [idx order]
                 (let [player (utils/find-first #(= order (:position %)) players)]
                   (assoc player :hand (set (get hands idx)))))
               deal-order)}))
