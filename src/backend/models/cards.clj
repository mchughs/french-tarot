(ns backend.models.cards
  (:require
   [backend.db :as db]))

(defn top-card
  "Returns the holding board entry."
  [board]
  (reduce
   (fn [incumbent challenger]
     (let [{c1 :board/card p1 :board/play-order} incumbent
           {c2 :board/card p2 :board/play-order} challenger]
       (cond
         (= :excuse (:type c1))
         challenger
         (= :excuse (:type c2))
         incumbent
         (and (= :trump (:type c1))
              (> (:value c1)
                 (:value c2)))
         incumbent
         (and (= :trump (:type c2))
              (< (:value c1)
                 (:value c2)))
         challenger
         (and (= (:suit c1) (:suit c2))
              (> (:value c1)
                 (:value c2)))
         incumbent
         (and (= (:suit c1) (:suit c2))
              (< (:value c1)
                 (:value c2)))
         challenger
         (and (not= (:suit c1) (:suit c2))
              (> p1 p2))
         challenger
         (and (not= (:suit c1) (:suit c2))
              (< p1 p2))
         incumbent)))
   board))

(defn remove-card-from-hand! [pid card]
  (db/run-fx! ::remove-card-from-hand pid card ))
