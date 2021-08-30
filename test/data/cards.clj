(ns cards)

(def ace-of-spades
  {:type :pip
   :value 1
   :points 0.5
   :suit :spades})

(def ten-of-spades
  {:type :pip
   :value 10
   :points 0.5
   :suit :spades})

(def jack-of-spades
  {:type :face
   :name :jack
   :value 11
   :points 1.5
   :suit :spades})

(def king-of-spades
  {:type :face
   :name :king
   :value 14
   :points 4.5
   :suit :spades})

(def ace-of-hearts
  {:type :pip
   :value 1
   :points 0.5
   :suit :hearts})

(def ten-of-hearts
  {:type :pip
   :value 10
   :points 0.5
   :suit :hearts})

(def jack-of-hearts
  {:type :face
   :name :jack
   :value 11
   :points 1.5
   :suit :hearts})

(def king-of-hearts
  {:type :face
   :name :king
   :value 14
   :points 4.5
   :suit :hearts})

(def the-petit
  {:type :trump
   :value 15
   :points 4.5
   :ouder? true})

(def the-13
  {:type :trump
   :value 27
   :points 0.5
   :ouder? false})

(def the-21
  {:type :trump
   :value 35
   :points 4.5
   :ouder? true})

(def the-excuse
  {:type :excuse
   :points 4.5
   :ouder? true})

(def iou
  {:type :iou
   :points 0.5
   :ouder? false})
