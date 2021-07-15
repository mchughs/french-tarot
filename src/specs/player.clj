(ns specs.player
  (:require [specs.cards :refer [Hand Pile]]
            [specs.round :refer [Bid Bonuses]]))

(def Player
  [:map [:id :uuid]
        [:name string?]
        [:score int?]
        [:position [:int {:min 0 :max 3}]]
        [:hand Hand]])

(def Defenders
  [:map [:members [:vector {:min 3 :max 3} Player]]
        [:pile Pile]])

(def Taker
  [:map [:bid Bid]
        [:player Player]
        [:pile Pile]
        [:bonuses Bonuses]])
