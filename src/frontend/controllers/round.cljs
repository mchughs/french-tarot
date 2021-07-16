(ns frontend.controllers.round
  (:require
   [clojure.set :as set]
   [frontend.websockets.core :as ws]
   [re-frame.core :as rf]
   [taoensso.sente :as sente]
   [utils :as utils]))

(rf/reg-fx
 :round/start
 (fn [{_rid :rid :as payload}]
   ((:send-fn ws/client-chsk)
    [:round/start payload]
    1000
    (fn [reply]
      (if (sente/cb-success? reply)
        (js/console.log "Success: started the round")
        (js/alert (str "Oops, you have a problem starting the round " reply "...")))))))

(rf/reg-fx
 :round/end
 (fn [{_rid :rid :as payload}]
   ((:send-fn ws/client-chsk)
    [:round/end payload]
    1000
    (fn [reply]
      (if (sente/cb-success? reply)
        (js/console.log "Success: ended the round")
        (js/alert (str "Oops, you have a problem ending the round " reply "...")))))))

(rf/reg-event-db
 :round/deal
 (fn [db [_ player-data]]
   (assoc db :player player-data)))

(rf/reg-sub
 ::hand
 (fn [db _]
   (get-in db [:player :hand])))

(rf/reg-event-fx
 ::start
 (fn [_ [_ rid]]
   {:round/start {:rid rid}}))

(rf/reg-event-fx
 ::end
 (fn [_ [_ rid]]
   {:round/end {:rid rid}}))

(defn sort-hand [hand]
  (let [sorted-ish (->> hand
                        (group-by :suit)
                        (utils/fmap (partial sort-by :value)))
        {:keys [spades
                hearts
                clubs
                diamonds
                trumps]} (set/rename-keys sorted-ish {nil :trumps})]
    (concat spades hearts clubs diamonds trumps)))

(comment
  (def round
    {:dog
     #{{:type :trump, :value 21, :points 0.5, :ouder? false}
       {:type :excuse, :points 4.5, :ouder? true}
       {:type :pip, :value 3, :points 0.5, :suit :hearts}
       {:type :pip, :value 10, :points 0.5, :suit :clubs}
       {:type :trump, :value 18, :points 0.5, :ouder? false}
       {:type :pip, :value 8, :points 0.5, :suit :diamonds}}
     :players
     ({:id "845ef68f-fd69-41e4-8ab0-57dda51f85c3"
       :name "Alice-430"
       :position 1
       :score 0
       :hand
       #{{:type :pip, :value 8, :points 0.5, :suit :spades}
         {:type :pip, :value 10, :points 0.5, :suit :hearts}
         {:type :pip, :value 2, :points 0.5, :suit :clubs}
         {:type :trump, :value 28, :points 0.5, :ouder? false}
         {:type :trump, :value 32, :points 0.5, :ouder? false}
         {:type :trump, :value 30, :points 0.5, :ouder? false}
         {:type :face, :name :queen, :value 13, :points 3.5, :suit :clubs}
         {:type :pip, :value 6, :points 0.5, :suit :clubs}
         {:type :pip, :value 5, :points 0.5, :suit :diamonds}
         {:type :pip, :value 6, :points 0.5, :suit :diamonds}
         {:type :trump, :value 19, :points 0.5, :ouder? false}
         {:type :face, :name :jack, :value 11, :points 1.5, :suit :spades}
         {:type :face, :name :knight, :value 12, :points 2.5, :suit :spades}
         {:type :pip, :value 2, :points 0.5, :suit :hearts}
         {:type :pip, :value 5, :points 0.5, :suit :clubs}
         {:type :trump, :value 24, :points 0.5, :ouder? false}
         {:type :trump, :value 31, :points 0.5, :ouder? false}
         {:type :face, :name :knight, :value 12, :points 2.5, :suit :clubs}}}
      {:id "3ece8db4-ad04-4fac-bca2-e73db2ff5134"
       :name "Charlie-284"
       :position 2
       :score 0
       :hand
       #{{:type :pip, :value 6, :points 0.5, :suit :hearts}
         {:type :pip, :value 1, :points 0.5, :suit :diamonds}
         {:type :face, :name :jack, :value 11, :points 1.5, :suit :diamonds}
         {:type :trump, :value 34, :points 0.5, :ouder? false}
         {:type :trump, :value 22, :points 0.5, :ouder? false}
         {:type :pip, :value 7, :points 0.5, :suit :diamonds}
         {:type :trump, :value 23, :points 0.5, :ouder? false}
         {:type :pip, :value 4, :points 0.5, :suit :hearts}
         {:type :face, :name :king, :value 14, :points 4.5, :suit :diamonds}
         {:type :trump, :value 15, :points 4.5, :ouder? true}
         {:type :face, :name :king, :value 14, :points 4.5, :suit :spades}
         {:type :pip, :value 1, :points 0.5, :suit :spades}
         {:type :pip, :value 8, :points 0.5, :suit :clubs}
         {:type :trump, :value 20, :points 0.5, :ouder? false}
         {:type :pip, :value 5, :points 0.5, :suit :spades}
         {:type :face, :name :queen, :value 13, :points 3.5, :suit :spades}
         {:type :trump, :value 26, :points 0.5, :ouder? false}
         {:type :face, :name :queen, :value 13, :points 3.5, :suit :hearts}}}
      {:id "a159a76a-e43e-4b65-8781-8d5a2b868c91"
       :name "Giselle-942"
       :position 3
       :score 0
       :hand
       #{{:type :pip, :value 6, :points 0.5, :suit :spades}
         {:type :pip, :value 4, :points 0.5, :suit :clubs}
         {:type :pip, :value 7, :points 0.5, :suit :hearts}
         {:type :pip, :value 10, :points 0.5, :suit :spades}
         {:type :pip, :value 9, :points 0.5, :suit :clubs}
         {:type :pip, :value 4, :points 0.5, :suit :spades}
         {:type :pip, :value 1, :points 0.5, :suit :hearts}
         {:type :pip, :value 2, :points 0.5, :suit :spades}
         {:type :pip, :value 4, :points 0.5, :suit :diamonds}
         {:type :pip, :value 9, :points 0.5, :suit :spades}
         {:type :trump, :value 16, :points 0.5, :ouder? false}
         {:type :pip, :value 3, :points 0.5, :suit :spades}
         {:type :face, :name :knight, :value 12, :points 2.5, :suit :hearts}
         {:type :pip, :value 3, :points 0.5, :suit :clubs}
         {:type :face, :name :king, :value 14, :points 4.5, :suit :clubs}
         {:type :pip, :value 9, :points 0.5, :suit :diamonds}
         {:type :pip, :value 3, :points 0.5, :suit :diamonds}
         {:type :pip, :value 7, :points 0.5, :suit :clubs}}}
      {:id "2eebc77f-694b-41c3-935e-38c32cbef01c"
       :name "Ivan-965"
       :position 0
       :score 0
       :hand
       #{{:type :trump, :value 27, :points 0.5, :ouder? false}
         {:type :face, :name :king, :value 14, :points 4.5, :suit :hearts}
         {:type :trump, :value 35, :points 4.5, :ouder? true}
         {:type :trump, :value 25, :points 0.5, :ouder? false}
         {:type :face, :name :queen, :value 13, :points 3.5, :suit :diamonds}
         {:type :face, :name :jack, :value 11, :points 1.5, :suit :clubs}
         {:type :pip, :value 1, :points 0.5, :suit :clubs}
         {:type :pip, :value 9, :points 0.5, :suit :hearts}
         {:type :pip, :value 8, :points 0.5, :suit :hearts}
         {:type :face, :name :knight, :value 12, :points 2.5, :suit :diamonds}
         {:type :face, :name :jack, :value 11, :points 1.5, :suit :hearts}
         {:type :pip, :value 2, :points 0.5, :suit :diamonds}
         {:type :pip, :value 7, :points 0.5, :suit :spades}
         {:type :trump, :value 29, :points 0.5, :ouder? false}
         {:type :trump, :value 17, :points 0.5, :ouder? false}
         {:type :pip, :value 5, :points 0.5, :suit :hearts}
         {:type :trump, :value 33, :points 0.5, :ouder? false}
         {:type :pip, :value 10, :points 0.5, :suit :diamonds}}})}))
