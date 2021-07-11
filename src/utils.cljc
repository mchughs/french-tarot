(ns utils
  (:require [clojure.pprint :as pp]))

(defn fmap
  "updates all the values in a map by f"
  [f m]
  (into {}
    (for [[k v] m]
      [k (f v)])))

(defn find-first
  [pred coll]
  (first (filter pred coll)))

(defn split-map-by-keys
  "takes a map and a coll of keys.
   returns a map just containing given kv-pairs and a second map containing all remaing kv-pairs."
  [m ks]
  [(select-keys m ks)
   (apply dissoc m ks)])

(defn pp-str
  "Like pr-str but for pretty printing."
  [x]
  (with-out-str (pp/pprint x)))
