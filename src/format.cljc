(ns format
  (:require #?@(:cljs [[goog.string :as gstring]
                       [goog.string.format]])
            [clojure.string :as string]))

#?(:clj  (defn fmt [& args] (apply format args))
   :cljs (defn fmt [& args] (apply gstring/format args)))

(defn card->name [{type :type :as card}]
  (case type
    :face (fmt "the %s of %s"
               (name (:name card))
               (name (:suit card)))
    :pip (fmt "the %s of %s"
              (let [v (:value card)]
                (if (= 1 v) "ace" v))
              (name (:suit card)))
    :trump (fmt "the %s"
                (let [v (- (:value card) 14)]
                  (if (= 1 v) "petit" v)))
    :excuse "the excuse"))
