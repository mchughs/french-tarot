(ns frontend.ui.elements.game-lobby-list
  (:require
   [frontend.ws :as ws]
   [re-frame.core :as rf]
   [reagent.core :as r]
   [taoensso.sente :as sente]))

(defn component []
  (r/with-let [games (rf/subscribe [:game/ids])]
    [:div
     (str "Found " (count @games) " existings games.")
     [:ul
      (->> @games
           (map (fn [guid]
                  ^{:key (gensym)}
                  [:li
                   [:button
                    {:on-click #((:send-fn ws/client-chsk)
                                 [:game/join {:guid guid}]
                                 1000
                                 (fn [reply]
                                   (js/console.log "Server replied with: " (pr-str reply))
                                   (if (sente/cb-success? reply)
                                     (js/console.log "Joining Game #" guid "...")
                                     (js/console.log "Couldn't Join Game # " guid "..."))))}
                    (str "Join Game #" guid)]]))
           doall)]]))
