(ns backend.models.user
  (:require [backend.db :as db]))

(defn- name-generator []
  (str (rand-nth ["Alice" "Bob" "Charlie" "David" "Eve" "Florence" "Giselle" "Harry" "Ivan" "Juliet" "Kevin" "Lisa"])
       "-"
       (rand-int 999)))

(defn create [uid]
  {:user/id    uid
   :user/name  (name-generator)
   :user/host? false})

(defn get-user
  "Returns the user associated with uid."
  [uid]  
  (db/q1 '{:find (pull user [*])
           :in [uid]
           :where [[user :user/id uid]]}
         uid))

(defn get-users
  "Returns all the users as a map."
  []
  (reduce
   (fn [acc [user]]
     (assoc acc (:user/id user) user))
   {}
   (db/q '{:find [(pull user [:user/id
                              :user/name
                              :user/room
                              :user/host?])]
           :where [[user :user/id]]})))

(defn host-room! [uid rid]
  (db/run-fx! ::join-room uid rid true))

(defn join-room! [uid rid]
  (db/run-fx! ::join-room uid rid false))

(defn leave-room! [uid rid]
  (db/run-fx! ::leave-room uid rid))
