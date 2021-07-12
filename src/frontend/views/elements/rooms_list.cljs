(ns frontend.views.elements.rooms-list
  (:require
   [frontend.lobby :as lobby]
   [re-frame.core :as rf]
   [reagent.core :as r]))

(defn component []
  (r/with-let [rooms (rf/subscribe [:rooms])]
    (let [cnt (count @rooms)]
      [:div
       (str "Found " cnt " existings rooms.")
       (when (pos? cnt)
         [:ul
          (->> @rooms
               (map (fn [[guid {connected-players :players host :host}]]
                      ^{:key (gensym "room")}
                      [:li
                       [:button {:disabled (<= 4 (count connected-players))
                                 :on-click #(rf/dispatch [::lobby/join guid])}
                        (str "Join Room #" guid " with " (count connected-players) "/4 players hosted by " host ".")]]))
               doall)])])))
