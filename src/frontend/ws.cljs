(ns frontend.ws
  (:require [taoensso.sente :as sente]))

(def ?csrf-token ;; TODO, won't be working yet.
  (when-let [el (.getElementById js/document "sente-csrf-token")]
    (.getAttribute el "data-csrf-token")))

(defonce client-chsk
  (sente/make-channel-socket-client!
   "/chsk"
   ?csrf-token
   {:type :auto}))

