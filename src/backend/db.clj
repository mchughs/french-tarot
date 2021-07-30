(ns backend.db
  (:require
   [crux.api :as crux]
   [mount.core :refer [defstate]]
   [clojure.java.io :as io]))

(defn- insert-tx-functions! [node]
  (crux/await-tx
   node
   (crux/submit-tx
    node
    [[:crux.tx/put
      {:crux.db/id :backend.models.round/next-dealer
       :crux.db/fn '(fn [ctx round-id]
                      (let [db (crux.api/db ctx)
                            round (crux.api/entity db round-id)]
                        [[:crux.tx/put (update round :round/dealer-turn #(mod (inc %) 4))]]))}]

     [:crux.tx/put
      {:crux.db/id :backend.models.game/end-round
       :crux.db/fn '(fn [ctx gid]
                      (let [db (crux.api/db ctx)
                            game (crux.api/entity db gid)]
                        [[:crux.tx/put (assoc game :game/status :pre)]]))}]

     [:crux.tx/put
      {:crux.db/id :backend.models.players/append-scores
       :crux.db/fn '(fn [ctx player-id score]
                      (let [db (crux.api/db ctx)
                            player (crux.api/entity db player-id)]
                        [[:crux.tx/put (update player :player/score + score)]]))}]
     [:crux.tx/put
      {:crux.db/id :backend.models.cards/remove-card-from-hand
       :crux.db/fn '(fn [ctx player-id card]
                      (let [db (crux.api/db ctx)
                            player (crux.api/entity db player-id)]
                        [[:crux.tx/put (update player :player/hand disj card)]]))}]

     [:crux.tx/put
      {:crux.db/id :backend.models.players/remove-dog-from-hand
       :crux.db/fn '(fn [ctx player-id init-pile]
                      (let [db (crux.api/db ctx)
                            player (crux.api/entity db player-id)]
                        [[:crux.tx/put (update player :player/hand clojure.set/difference init-pile)]]))}]

     [:crux.tx/put
      {:crux.db/id :backend.models.players/add-dog-to-hand
       :crux.db/fn '(fn [ctx player-id dog]
                      (let [db (crux.api/db ctx)
                            player (crux.api/entity db player-id)]
                        [[:crux.tx/put (update player :player/hand clojure.set/union dog)]]))}]

     [:crux.tx/put
      {:crux.db/id :backend.models.round/append-log
       :crux.db/fn '(fn [ctx round-id log-id]
                      (let [db (crux.api/db ctx)
                            round (crux.api/entity db round-id)]
                        [[:crux.tx/put (update round :round/logs conj log-id)]]))}]

     [:crux.tx/put
      {:crux.db/id :backend.models.game/begin-round
       :crux.db/fn '(fn [ctx gid]
                      (let [db (crux.api/db ctx)
                            game (crux.api/entity db gid)]
                        [[:crux.tx/put (assoc game :game/status :during)]]))}]

     [:crux.tx/put
      {:crux.db/id :backend.models.deck/set-dog
       :crux.db/fn '(fn [ctx round-id dog]
                      (let [db (crux.api/db ctx)
                            round (crux.api/entity db round-id)]
                        [[:crux.tx/put (assoc round :round/dog dog)]]))}]

     [:crux.tx/put
      {:crux.db/id :backend.models.deck/deal
       :crux.db/fn '(fn [ctx player-id hand]
                      (let [db (crux.api/db ctx)
                            player (crux.api/entity db player-id)]
                        [[:crux.tx/put (assoc player :player/hand hand)]]))}]

     [:crux.tx/put
      {:crux.db/id :backend.models.round/start
       :crux.db/fn '(fn [ctx gid round-id]
                      (let [db (crux.api/db ctx)
                            game (crux.api/entity db gid)]
                        [[:crux.tx/put (update game :game/rounds conj round-id)]]))}]

     [:crux.tx/put
      {:crux.db/id :backend.models.game/start
       :crux.db/fn '(fn [ctx rid gid]
                      (let [db (crux.api/db ctx)
                            room (crux.api/entity db rid)]
                        [[:crux.tx/put (-> room
                                           (assoc :room/status :closed)
                                           (assoc :room/game gid))]]))}]

     [:crux.tx/put
      {:crux.db/id :backend.models.user/join-room
       :crux.db/fn '(fn [ctx uid rid host?]
                      (let [db (crux.api/db ctx)
                            user (crux.api/entity db uid)]
                        [[:crux.tx/put (-> user
                                           (assoc :user/room rid)
                                           (assoc :user/host? host?))]]))}]

     [:crux.tx/put
      {:crux.db/id :backend.models.user/leave-room
       :crux.db/fn '(fn [ctx uid rid]
                      (let [db (crux.api/db ctx)
                            user (crux.api/entity db uid)]
                        [[:crux.tx/put (-> user
                                           (dissoc :user/room)
                                           (assoc :user/host? false))]]))}]

     [:crux.tx/put
      {:crux.db/id :backend.models.room/add-player
       :crux.db/fn '(fn [ctx rid uid username]
                      (let [db (crux.api/db ctx)
                            room (crux.api/entity db rid)
                            full? (= 3 (count (:room/players room)))]
                        [[:crux.tx/put (cond-> room
                                         true
                                         (update :room/players conj uid)
                                         true
                                         (update :room/playernames assoc uid username)
                                         full?
                                         (assoc :room/status :full))]]))}]
     [:crux.tx/put
      {:crux.db/id :backend.models.room/remove-player
       :crux.db/fn '(fn [ctx rid uid]
                      (let [db (crux.api/db ctx)
                            room (crux.api/entity db rid)]
                        [[:crux.tx/put (-> room
                                           (update :room/players disj uid)
                                           (update :room/playernames dissoc uid)
                                           (assoc :room/status :open))]]))}]

     [:crux.tx/put
      {:crux.db/id :backend.models.user/give-name
       :crux.db/fn '(fn [ctx uid name]
                      (let [db (crux.api/db ctx)
                            user (crux.api/entity db uid)]
                        [[:crux.tx/put (assoc user :user/name name)]]))}]])))

(defn start! []
  (letfn [(kv-store [dir]
            {:kv-store {:crux/module 'crux.rocksdb/->kv-store
                        :db-dir      (io/file dir)
                        :sync?       true}})]
    (let [node (crux/start-node
                {#_#_:crux/tx-log         (kv-store "data/dev/tx-log")
                 #_#_:crux/document-store (kv-store "data/dev/doc-store")
                 #_#_:crux/index-store    (kv-store "data/dev/index-store")})]
      (insert-tx-functions! node)
      node)))

(defstate node
  :start (start!)
  :stop (when node
          (.close node)))

(defn q [query & args]
  (apply crux/q (crux/db node) query args))

(defn q1
  "If you just need to return one value from the query,
   and you don't want it wrapped in a vec, use this."
  [query & args]
  (let [return-tuples (vector? (:find query))
        query (cond-> query
                (not return-tuples)
                (update :find vector))
        results (apply crux/q (crux/db node) query args)]
    (cond->> results
      (not return-tuples)
      (map first)
      true
      first)))

(defn insert!
  "Takes an entity id and a document."
  [id doc]
  (crux/submit-tx
   node 
   [[:crux.tx/put (merge doc {:crux.db/id id})]]))

(defn delete!
  "Takes an entity id and deletes it."
  [id]
  (crux/submit-tx
   node
   [[:crux.tx/delete id]]))

(defn exists?
  "Checks if a given entity exists in the DB."
  [id]
  (seq (crux/entity-history (crux/db node) id :desc)))

(defn run-fx!
  "Takes a `fn-key` for a transaction function already present in the crux db.
   Also takes the `args` to provide to that function.
   Since we're updating a document, and are likely to immediately fetch, we await the tx."
  [fn-key & args]
  (crux/await-tx node (crux/submit-tx node [(into [:crux.tx/fn fn-key] args)])))

(comment
  (q '{:find [(pull e [*])]
       :where [[e :crux.db/id]]})

  (q '{:find [(pull e [*])]
       :where [[e :game/id]]})

  (q '{:find [(pull e [*])]
       :where [[e :user/id]]})

  (q '{:find [(pull e [*])]
       :where [[e :player/id]]})

  (->> (q '{:find [(pull e [*])]
            :where [[e :player/id]]})
       (map (comp count :player/hand first)))

  (q '{:find [(pull e [*])]
       :where [[e :room/id]]})

  (q '{:find [(pull e [*])]
       :where [[e :round/id]]})  

  (->> (q '{:find [(pull e [*])]
            :where [[e :log/id]]})
       (map first)
       (sort-by :log/idx))

  (map (comp :player/name first)
       (q '{:find [(pull e [*])]
            :where [[e :player/id]]}))
  )
