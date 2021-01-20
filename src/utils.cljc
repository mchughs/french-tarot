(ns utils)

(defn fmap
  "updates all the values in a map by f"
  [f m]
  (into {}
    (for [[k v] m]
      [k (f v)])))

(defn find-first
  [pred coll]
  (prn coll)
  (first (filter pred coll)))
