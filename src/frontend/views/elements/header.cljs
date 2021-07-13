(ns frontend.views.elements.header
  (:require [reitit.frontend.easy :as rfe]
            [re-frame.core :as rf]
            [reagent.core :as r]))

(defn tabs [rid?]
  [{:label "Homepage" :name :router/home}
   (when rid? {:label "Room" :name :router/room-lobby})
   {:label "About" :name :router/about}])

(defn component [selected-tab]
  (r/with-let [rid (rf/subscribe [:committed-room])
               player-name (rf/subscribe [:player-name])]
    [:header
     [:div.pb-5.border-b.border-gray-200.sm:pb-0
      [:h3.text-lg.leading-6.font-medium.text-gray-900 "French Tarrot"]
      [:h4.text-md.leading-6.font-medium.text-gray-700 "Username: " @player-name]
      [:div.mt-3.sm:mt-4
       [:div.sm:hidden
        [:label.sr-only {:for "current-tab"} "Select a tab"]
        [:select#current-tab.block.w-full.pl-3.pr-10.py-2.text-base.border-gray-300.focus:outline-none.focus:ring-indigo-500.focus:border-indigo-500.sm:text-sm.rounded-md
         {:name "current-tab"
          :value selected-tab
          :on-change (fn [e] ;; annoyingly the keyword in value loses it's ns on click so we have to add it back in here.
                       (let [route (keyword "router" (.. e -target -value))]
                         (case route
                           :router/room-lobby (rfe/push-state route {:rid @rid})
                           (rfe/push-state route))))}
         (->> (tabs @rid)
              (map (fn [{:keys [label name]}]
                     ^{:key (gensym)}
                     [:option {:value name} label]))
              doall)]]
       [:div.hidden.sm:block
        [:nav.-mb-px.flex.space-x-8
         (->> (tabs @rid)
              (map (fn [{:keys [label name]}]
                     (let [href (case name
                                  :router/room-lobby (rfe/href name {:rid @rid})
                                  (rfe/href name))]
                       ^{:key (gensym)}
                       [:a.whitespace-nowrap.pb-4.px-1.border-b-2.font-medium.text-sm
                        {:class (if (= selected-tab name)
                                  "border-indigo-500 text-indigo-600"
                                  "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300")
                         :href href
                         :aria-current (when (= selected-tab name) "page")}
                        label])))
              doall)]]]]]))
