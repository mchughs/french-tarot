(ns frontend.views.elements.rooms-list
  (:require
   [frontend.lobby :as lobby]
   [re-frame.core :as rf]
   [reagent.core :as r]))

(defn component [committed-room]
  (r/with-let [rooms (rf/subscribe [:rooms])]
    [:div.max-w-max
     [:div.flex.flex-col
      [:div.-my-2.overflow-x-auto.sm:-mx-6.lg:-mx-8
       [:div.py-2.align-middle.inline-block.min-w-full.sm:px-6.lg:px-8
        [:div.shadow.overflow-hidden.border-b.border-gray-200.sm:rounded-lg
         [:table.min-w-full.divide-y.divide-gray-200
          [:thead.bg-gray-50
           [:tr
            [:th.px-6.py-3.text-left.text-xs.font-medium.text-gray-500.uppercase.tracking-wider
             {:scope "col"}
             "Host"]
            [:th.px-6.py-3.text-left.text-xs.font-medium.text-gray-500.uppercase.tracking-wider
             {:scope "col"}
             "Number of Players"]
            [:th.relative.px-6.py-3
             {:scope "col"}
             [:span.sr-only "Join Game Button"]]]]
          [:tbody
           (->> @rooms
                (map (fn [[rid {connected-players :players host :host}]]
                       ^{:key (gensym)}
                       [:tr.bg-white
                        [:td.px-6.py-4.whitespace-nowrap.text-sm.text-gray-500
                         @(rf/subscribe [:player-name host])]
                        [:td.px-6.py-4.whitespace-nowrap.text-sm.text-gray-500
                         (str (count connected-players) "/4")]
                        [:td.px-6.py-4.whitespace-nowrap.text-left.text-sm.font-medium
                         [:button {:class (when (= committed-room rid) "blue")
                                   :disabled (<= 4 (count connected-players))
                                   :on-click #(rf/dispatch [::lobby/join rid])}
                          (if (= committed-room rid)
                            "Return"
                            "Join!")]]]))
                doall)]]]
        (when-not
         (pos? (count @rooms))
          [:div.bg-white.px-6.py-4.whitespace-nowrap.text-lg.text-gray-500
           "No games found 😔"])]]]]))
