(ns specs.round
  (:require [malli.util :as mu]))

(def Bid
  [:enum :bid/petit
         :bid/garde
         :bid/garde-sans
         :bid/garde-contre])

(def Bonuses
  (mu/optional-keys
    [:map [:bonus/won-petit-au-bout   [:= 10]
           :bonus/lost-petit-au-bout  [:= -10]
           :bonus/single-handful      [:= 20]
           :bonus/double-handful      [:= 40]
           :bonus/triple-handful      [:= 60]
           :bonus/slam                [:= 200]]]))
     ;TODO :bonus/announced-slam :value 400 :penalty -200

;TODO Misere
; (def IndividualBonuses
;   [:set [:enum {:type :bonus/misery :value 10}]])
                 ; {:type :bonus/single-handful :value 20}
                 ; {:type :bonus/double-handful :value 40}
                 ; {:type :bonus/triple-handful :value 60}}]])

(def Phase
  [:enum :bidding :announcements :dog-construction :main :scoring :end])
