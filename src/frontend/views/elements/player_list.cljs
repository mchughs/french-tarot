(ns frontend.views.elements.player-list)

(defn component [players]
  (let [open-spots (- 4 (count players))]
    [:ul
     (->> players
          (map (fn [uid]
                 ^{:key (gensym "player")}
                 [:li (str "Player #" uid)]))
          doall)
     (->> (range open-spots)
          (map
             (fn [idx]
               ^{:key (str "open-spot/" idx)}
               [:li ":"]))
          doall)]))
