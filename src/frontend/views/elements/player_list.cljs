(ns frontend.views.elements.player-list)

(defn list-item [& [{:keys [name host? you?]
                     :or {name "_"
                          host? false
                          you? false}}]]
  [:li.py-4.flex
   [:div.ml-3
    [:p.text-sm.font-medium.text-gray-900
     {:class (when you? "text-purple-700 text-opacity-100")}
     name]
    (when host?
      [:p.text-sm.text-gray-500
       "Room Host"])]])

(defn component [player-id host-id players playernames]
  (let [open-spots (- 4 (count players))]
    [:ul.divide-y.divide-gray-200
     (->> players
          (map (fn [uid]
                 ^{:key (gensym)}
                 [list-item
                  {:name (get playernames uid)
                   :host? (= host-id uid)
                   :you? (= player-id uid)}]))
          doall)
     (->> (range open-spots)
          (map (fn [_] ^{:key (gensym)} [list-item]))
          doall)]))
