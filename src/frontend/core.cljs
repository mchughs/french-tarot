(ns frontend.core
  (:require
   [frontend.events :as ev]
   [frontend.lobby :as lobby]
   [frontend.router :as router]
   [frontend.views.pages.not-found :as not-found]
   [re-frame.core :as rf]
   [reagent.core :as r]
   [reagent.dom :as rdom]
   frontend.subscriptions))

(defn component []
  (r/with-let [match (rf/subscribe [:page/match])]
    [:div#root
     (if @match
       (let [view (:view (:data @match))]
         (when @(rf/subscribe [:chsk/open?]) ;; wait for the channel socket to be open
           [view @match]))
       [not-found/page])]))

(defn init-db! []
  (js/console.log "Init re-frame DB...")
  (rf/dispatch [::ev/load-page]))

(defn mount! []
  (js/console.log "Mounting...")
  (rdom/render [component]
               (js/document.getElementById "app")))

(defn add-channel-socket-watcher! []
  (add-watch
   (rf/subscribe [:chsk/open?]) ;; Needs to be deref somewhere later on or the callback is never called.
   :chsk/open
   (fn [_key _atom old-state new-state]
     (when (and (not= old-state new-state) new-state)
       (lobby/fetch-rooms!)))))

(defn init []
  (js/console.log "Init Frontend...")
  (init-db!)
  (add-channel-socket-watcher!)
  (router/start!)
  (mount!))

(comment
  (init))
