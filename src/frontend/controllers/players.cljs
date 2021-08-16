(ns frontend.controllers.players
  (:require
   [re-frame.core :as rf]
   [utils :as utils]))

(rf/reg-event-db
 ::update
 (fn [db [_ player]]
   (assoc db :player player)))

(rf/reg-sub
 ::order
 (fn [db _]
   (sort-by :position (:players db))))

(rf/reg-sub
 ::player
 (fn [db _]
   (:player db)))

(rf/reg-sub
 ::hand
 :<- [::player]
 (fn [player _]
   (get player :player/hand)))

(rf/reg-sub
 ::score
 :<- [::player]
 (fn [player _]
   (get player :player/score)))

;; Other players
;; TODO fix it
(rf/reg-event-db
 ::fetch
 (fn [db [_ players]]
   (let [bottom-player (utils/find-first #(= (:user/id db)
                                             (:player/user-id %))
                                         players)
         left-player (utils/find-first #(= (:player/position bottom-player)
                                           (mod (dec (:player/position %)) 4))
                                       players)
         top-player (utils/find-first #(= (:player/position left-player)
                                          (mod (dec (:player/position %)) 4))
                                      players)
         right-player (utils/find-first #(= (:player/position top-player)
                                            (mod (dec (:player/position %)) 4))
                                        players)]
     (assoc db :players {:bottom bottom-player
                         :left left-player
                         :top top-player
                         :right right-player}))))

(rf/reg-sub
 ::k-player
 (fn [db [_ position-key]]
   (get-in db [:players position-key])))

(rf/reg-sub
 ::player-turn?
 (fn [db [_ position-key]]
   (= (get-in db [:players position-key :player/position])
      (get-in db [:curr/log :log/player-turn]))))

(rf/reg-sub
 ::taker-id
 (fn [db _]
   (get-in db [:curr/log :log/taker :uid])))

(rf/reg-sub
 ::taker-pile
 (fn [db _]
   (get-in db [:curr/log :log/taker :pile])))

(rf/reg-sub
 ::defenders-pile
 (fn [db _]
   (get-in db [:curr/log :log/defenders :pile])))
