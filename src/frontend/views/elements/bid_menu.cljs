(ns frontend.views.elements.bid-menu
  (:require
   [frontend.controllers.log :as log]
   ["@material-ui/core/Tooltip" :default Tooltip]
   ["@material-ui/core/styles" :refer (withStyles)]
   [re-frame.core :as rf]))

(def bid-options
  [:bid/petit
   :bid/garde
   :bid/garde-sans
   :bid/garde-contre])

(def bid->description
  {:bid/petit "The smallest bid. The taker gets the 'dog' and sets aside 6 cards in their pile."
   :bid/garde "The standard bid. The taker gets the 'dog' and sets aside 6 cards in their pile. The taker will win/lose twice as much points compared to if they bid a 'petit'."
   :bid/garde-sans "A confident bid. The taker gets the 'dog' but immediately puts it aside into their pile without looking. The taker will win/lose more points compared to if they bid a 'garde'."
   :bid/garde-contre "The most confident bid. The defenders get the 'dog' but immediately puts it aside into their pile without looking. The taker will win/lose even more points compared to if they bid a 'garde-sans'."})

(def StyledTooltip
  ((withStyles (fn [_theme]
                 #js {"tooltip" #js {"font-size" "20px"}}))
   Tooltip))

(defn tile [bid]  
  [:> StyledTooltip
   {:class "tooltip"
    :title (bid->description bid)
    :arrow true}
   [:span (name bid)]])

(defn component [available-bids]
  [:<>
   [:ul.grid.grid-cols-2.grid-rows-2
    (->> bid-options
         (map (fn [option]
                ^{:key (gensym)}
                (let [available-bid? (contains? (set available-bids)
                                                option)]
                  [:li.place-self-center
                   [:button.mx-2
                    {:on-click #(when available-bid?
                                  (rf/dispatch [::log/place-bid option]))
                     :disabled (not available-bid?)}
                    [tile option]]])))
         doall)]
   [:div.grid.place-items-center
    [:button.mt-2 {:on-click #(rf/dispatch [::log/place-bid :pass])}
     "pass"]]])
