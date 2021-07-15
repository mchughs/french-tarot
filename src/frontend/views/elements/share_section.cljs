(ns frontend.views.elements.share-section
  (:require [reagent.core :as r]))

(defn component [full?]
  (r/with-let [copied? (r/atom false)]
    (when-not full?
      [:div.grid
       [:div.font-normal.text-md.justify-self-center "Invite a friend by copying the link below"]
       [:button.basic.flex.flex-row.border-4.justify-self-center.relative.mt-2
        {:title (if @copied? "Copied!" "Click to Copy")
         :class (if @copied? "border-green-600" "border-indigo-600")
         :on-click
         #(-> js/navigator
              .-clipboard
              (.writeText js/window.location.href)
              (.then (fn [] (reset! copied? true))
                     (fn [err] (js/alert "Sorry, we experienced an error: " err))))}
        [:div.self-center.pl-1.py-2 js/window.location.href]
        (if-not @copied?
          [:img.self-center {:src "https://img.icons8.com/dotty/32/000000/clipboard.png"}]
          [:img.self-center {:src "https://img.icons8.com/color/32/000000/pass.png"}])
        [:div.absolute.self-center.bg-white.bg-opacity-100.w-full.h-full.flex.justify-center.hover:opacity-0
         [:div.self-center "Hover Me!"]]]])))
