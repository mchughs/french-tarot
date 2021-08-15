(ns frontend.views.components.card
  (:require
   [clojure.string :as s]
   [format :as fmt]
   [frontend.controllers.card :as card]
   [frontend.controllers.log :as log]
   [re-frame.core :as rf]
   [reagent.core :as r]))

(defn back []
  [:div.card   
   [:img {:src (fmt/fmt "http://localhost:5444/assets/images/cards_new/cardback_1.jpg")
          :title "back"}]])

(defn component [card card-class]
  (r/with-let [phase (rf/subscribe [::log/phase])
               user-turn? (rf/subscribe [::log/user-turn?])
               init-taker-pile (rf/subscribe [::card/init-taker-pile])
               taker? (rf/subscribe [::log/taker?])]
    [:li.card {:class (str (or card-class "play-area-card") " "
                           (when (contains? @init-taker-pile card)
                             "set-aside"))
               :disabled (and (<= 6 (count @init-taker-pile))
                              (not (contains? @init-taker-pile card)))
               :on-click (cond (and (= :dog-construction @phase)
                                    @taker?
                                    (contains? @init-taker-pile card))
                               #(rf/dispatch [::card/recover card])

                               (and (= :dog-construction @phase)
                                    @taker?)
                               #(rf/dispatch [::card/set-aside card])

                               (and (= :main @phase)
                                    @user-turn?)
                               #(rf/dispatch [::card/play card])

                               :else
                               #(js/console.log "TODO???"))}
     [:img {:src (fmt/fmt "http://localhost:5444/assets/images/cards_new/%s.jpg"
                          (s/replace (fmt/card->name card) #"\s" "_"))
            :title (fmt/card->name card)}]]))
