(ns backend.models.user
  (:require [backend.db :as db]))

(defn create [uid]
  {:user/id    uid   
   :user/host? false})

(defn get-user
  "Returns the user associated with uid."
  [uid]  
  (db/q1 '{:find (pull user [*])
           :in [uid]
           :where [[user :user/id uid]]}
         uid))

(defn exists?
  "Returns the true if a user is found associated with uid."
  [uid]
  (not (nil? (db/q1 '{:find (pull user [*])
                      :in [uid]
                      :where [[user :user/id uid]]}
                    uid))))

(defn get-username
  [uid]
  (db/q1 '{:find username
           :in [uid]
           :where [[e :user/id uid]
                   [e :user/name username]]}
         uid))

(defn has-name? [uid username]
  (= username
     (get-username uid)))

(defn name-taken?
  [username]
  (db/q1 '{:find uid
           :in [username]
           :where [[e :user/id uid]
                   [e :user/name username]]}
         username))

(defn give-name! [uid name]
  (db/run-fx! ::give-name uid name))

(defn host-room! [uid rid]
  (db/run-fx! ::join-room uid rid true))

(defn join-room! [uid rid]
  (db/run-fx! ::join-room uid rid false))

(defn leave-room! [uid rid]
  (db/run-fx! ::leave-room uid rid))

(defn clear-users! []
  (let [uids (db/q '{:find [e]
                     :where [[e :user/id]]})]
    (doseq [[uid] uids]
      (db/run-fx! ::clean uid))))

