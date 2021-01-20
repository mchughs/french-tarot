(ns specs.cards
  (:require [malli.util :as mu]))

(def Card
  [:multi {:dispatch :type}
   [:face   [:map [:type   keyword?]
                  [:name   [:enum :jack :knight :queen :king]]
                  [:points [:enum 1.5 2.5 3.5 4.5]]
                  [:value  [:and pos-int? [:>= 11] [:<= 14]]]
                  [:suit   [:enum :hearts :diamonds :clubs :spades]]]]
   [:pip    [:map [:type   keyword?]
                  [:points [:= 0.5]]
                  [:value  [:and pos-int? [:<= 10]]]
                  [:suit   [:enum :hearts :diamonds :clubs :spades]]]]
   [:trump  [:map [:type   keyword?]
                  [:points [:enum 0.5 4.5]]
                  [:value  [:and pos-int? [:>= 15] [:<= 35]]]
                  [:ouder? boolean?]]]
   [:excuse [:map [:type   keyword?]
                  [:points [:= 4.5]]
                  [:ouder? [:= true]]]]])

(def Dog  [:set {:min 6 :max 6}  Card])
(def Hand [:set {:min 0 :max 18} Card])
(def Pile [:vector {:min 0 :max 78}  Card])
(def Deck [:vector {:min 78 :max 78} Card])
