(ns backend.models.cards)

(defn top-card [board]
  (reduce
   (fn [incumbent challenger]
     (let [{c1 :card p1 :play-order} incumbent
           {c2 :card p2 :play-order} challenger]
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
