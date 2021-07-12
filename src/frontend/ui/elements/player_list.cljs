(ns frontend.ui.elements.player-list)

(defn component [players]
  (let [n (count players)]
    [:ul
     (->> players
          (map (fn [uid]
                 ^{:key (gensym)}
                 [:li (str "Player #" uid)]))
          doall)
     (repeat (- 4 n) ^{:key (gensym)}[:li ":"])]))
