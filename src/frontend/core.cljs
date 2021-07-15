(ns frontend.core
  (:require
   [frontend.events :as events]
   [frontend.controllers.lobby :as lobby]
   [frontend.router.core :as router]
   [frontend.views.pages.not-found :as not-found]
   [frontend.views.elements.header :as header]
   [re-frame.core :as rf]
   [reagent.core :as r]
   [reagent.dom :as rdom] 
   [frontend.websockets.subscriptions :as ws.subs]
   [frontend.router.subscriptions :as router.subs]))

(defn component []
  (r/with-let [match (rf/subscribe [::router.subs/page-match])]
    [:div#root
     (if @match
       (let [{view :view tab-name :name} (:data @match)]
         [:div {:class "bg-gray-200 grid overflow-hidden sm:rounded-lg"}
          [:div {:class "bg-white px-4 py-5 sm:p-6 h-screen max-w-screen-lg justify-self-center w-full"}
           [header/component tab-name]
           (when @(rf/subscribe [::ws.subs/chsk-open?]) ;; wait for the channel socket to be open           
             [view @match])]])
       [not-found/page])]))

(defn init-db! []
  (js/console.log "Init re-frame DB...")
  (rf/dispatch [::events/init-db]))

(defn mount! []
  (js/console.log "Mounting...")
  (rdom/render [component]
               (js/document.getElementById "app")))

(defn add-channel-socket-watcher! []
  (add-watch
   (rf/subscribe [::ws.subs/chsk-open?]) ;; Needs to be deref somewhere later on or the callback is never called.
   :chsk/open
   (fn [_key _atom old-state new-state]
     (when (and (not= old-state new-state) new-state)
       (lobby/fetch-names!) ;; For illustrative purposes only
       (lobby/fetch-rooms!)))))

(defn init []
  (js/console.log "Init Frontend...")
  (init-db!)
  (add-channel-socket-watcher!)
  (router/start!)
  (mount!))

(comment
  (init))
