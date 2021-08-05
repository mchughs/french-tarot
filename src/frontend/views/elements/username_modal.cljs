(ns frontend.views.elements.username-modal
  (:require
   [frontend.controllers.user :as user]
   [reagent.core :as r]
   [re-frame.core :as rf]
   ["react" :refer (Fragment)]
   ["@headlessui/react" :refer (Dialog Transition)]))

(defn component []
  (r/with-let [missing-name? (rf/subscribe [::user/missing-name?])
               username (r/atom nil)]
    (when @missing-name?
      [:> (.-Root Transition)
       {:show @missing-name?
        :as Fragment}
       [:> Dialog
        {:as "div"
         :class "fixed z-10 inset-0 overflow-y-auto"
         :open @missing-name?
         :on-close #(js/console.log "Closing username modal...")}
        [:div.flex.items-end.justify-center.min-h-screen.pt-4.px-4.pb-20.text-center.sm:block.sm:p-0
         [:> (.-Child Transition)
          {:as Fragment
           :enter "ease-out duration-300"
           :enterFrom "opacity-0"
           :enterTo "opacity-100"
           :leave "ease-in duration-200"
           :leaveFrom "opacity-100"
           :leaveTo "opacity-0"}
          [:> (.-Overlay Dialog)
           {:class "fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity"}]]
         [:span.hidden.sm:inline-block.sm:align-middle.sm:h-screen
          {:aria-hidden "true"}
          "​"]
         [:> (.-Child Transition)
          {:as Fragment
           :enter "ease-out duration-300"
           :enterFrom "opacity-0 translate-y-4 sm:translate-y-0 sm:scale-95"
           :enterTo "opacity-100 translate-y-0 sm:scale-100"
           :leave "ease-in duration-200"
           :leaveFrom "opacity-100 translate-y-0 sm:scale-100"
           :leaveTo "opacity-0 translate-y-4 sm:translate-y-0 sm:scale-95"}
          [:div.inline-block.align-bottom.bg-white.rounded-lg.px-4.pt-5.pb-4.text-left.overflow-hidden.shadow-xl.transform.transition-all.sm:my-8.sm:align-middle.sm:max-w-sm.sm:w-full.sm:p-6
           [:div
            [:div.mt-3.text-center.sm:mt-5
             [:> (.-Title Dialog)
              {:as "h3" :class "text-lg leading-6 font-medium text-gray-900"}
              "Choose a Username"]
             [:div.mt-2
              [:p.text-sm.text-gray-500
               "Username should be x y z"]]]]
           [:div.mt-5.sm:mt-6
            [:input.w-full {:type "text" :on-change #(reset! username (.. % -target -value))}]]
           [:div.mt-5.sm:mt-6
            [:button.basic.inline-flex.justify-center.w-full.rounded-md.border.border-transparent.shadow-sm.px-4.py-2.bg-indigo-600.text-base.font-medium.text-white.hover:bg-indigo-700.focus:outline-none.focus:ring-2.focus:ring-offset-2.focus:ring-indigo-500.sm:text-sm
             {:type "button"
              :on-click #(rf/dispatch [::user/submit-name @username])}
             "Submit"]]]]]]])))
