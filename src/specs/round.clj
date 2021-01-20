(ns specs.round)

(def Bid
  [:enum {:type :petite       :multiplier 1}
         {:type :garde        :multiplier 2}
         {:type :garde-sans   :multiplier 4}
         {:type :garde-contre :multiplier 6}])
