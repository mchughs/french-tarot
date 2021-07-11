(ns frontend.ui.elements.game-lobby-list
  (:require
   [frontend.lobby :as lobby]
   [re-frame.core :as rf]
   [reagent.core :as r]))

(defn component []
  (r/with-let [games (rf/subscribe [:games])]
    [:div
     (str "Found " (count @games) " existings games.")
     [:ul
      (->> @games
           (map (fn [[guid connected-players]]
                  ^{:key (gensym)}
                  [:li
                   [:button {:disabled (<= 4 (count connected-players))
                             :on-click #(rf/dispatch [::lobby/join guid])}
                    (str "Join Game #" guid " with " (count connected-players) "/4 players.")]]))
           doall)]]))
