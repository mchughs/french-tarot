(ns frontend.views.elements.rooms-list
  (:require
   [frontend.controllers.players :as players]
   [frontend.controllers.room :as room]
   [re-frame.core :as rf]
   [reagent.core :as r]))

(defn component [user-room]
  (r/with-let [rooms (rf/subscribe [::room/rooms])]
    [:div.max-w-max.py-5
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
                (map (fn [[rid {connected-players :room/players
                                host :room/host
                                status :room/status}]]
                       (let [participant? (= user-room rid)
                             closed? (= :closed status)
                             full? (= :full status)]
                         ^{:key (gensym)}
                         [:tr.bg-white
                          [:td.px-6.py-4.whitespace-nowrap.text-sm.text-gray-500
                           @(rf/subscribe [::players/name host])]
                          [:td.px-6.py-4.whitespace-nowrap.text-sm.text-gray-500
                           (str (count connected-players) "/4")]
                          [:td.px-6.py-4.whitespace-nowrap.text-left.text-sm.font-medium
                           [:button {:class (when (or participant? closed?)
                                              "blue")
                                     :disabled (and full?
                                                    ;; TODO fix socket event failing to let participating players in.
                                                    (not participant?)) ;; let participating players use the "Return" button
                                     :on-click (if (not participant?)
                                                 #(rf/dispatch [::room/join rid])
                                                 #(rf/dispatch [::room/enter {:rid rid}]))}
                            (cond closed? "In Progress"
                                  participant? "Return"
                                  :else "Join!")]]])))
                doall)]]]
        (when-not
         (pos? (count @rooms))
          [:div.bg-white.px-6.py-4.whitespace-nowrap.text-lg.text-gray-500
           "No games found 😔"])]]]]))
