(ns super-dice-roll.db
  (:require [honey.sql :as sql]
            [honey.sql.helpers :as sql.helpers]
            [parenthesin.components.database :as components.database]
            [schema.core :as s]
            [super-dice-roll.adapters :as base.adapters]
            [super-dice-roll.schemas.db :as schemas.db]
            [super-dice-roll.schemas.models :as schemas.models]
            [super-dice-roll.schemas.types :as schemas.types]))

(s/defn ^:private insert-new-roll*!
  [{:rolls/keys [each] :as roll} :- schemas.db/NewRoll
   db :- schemas.types/DatabaseComponent]
  (let [sql-command (-> (sql.helpers/insert-into :rolls)
                        (sql.helpers/values
                         [(assoc roll :rolls/each [:cast each :json])])
                        (sql.helpers/returning :*)
                        sql/format)]
    (->> sql-command
         (components.database/execute db)
         first)))

(s/defn insert-new-roll!
  [rolled :- schemas.models/Rolled
   db :- schemas.types/DatabaseComponent]
  (-> rolled
      base.adapters/rolled->db-new-roll
      (insert-new-roll*! db)))

(s/defn ^:private get-user-channel-rolls* :- [schemas.db/Roll]
  [user-id :- s/Str
   channel-id :- s/Int
   db :- schemas.types/DatabaseComponent]
  (let [sql-command (-> (sql.helpers/select :id
                                            :user_id
                                            :channel_id
                                            :command
                                            :modifier
                                            :total
                                            :each
                                            :created_at)
                        (sql.helpers/from :rolls)
                        (sql.helpers/where :and
                                           [:= :user_id user-id]
                                           [:= :channel_id channel-id])
                        (sql.helpers/limit 10)
                        sql/format)
        results (components.database/execute db sql-command)]
    (map (fn [{:rolls/keys [each] :as roll}]
           (assoc roll :rolls/each (.getValue each)))
         results)))

(s/defn get-user-channel-rolls :- schemas.models/UserCommandHistory
  [{:keys [id channel] :as user} :- schemas.models/User
   db :- schemas.types/DatabaseComponent]
  (-> id
      (get-user-channel-rolls* (channel schemas.models/ChannelDefinition) db)
      (base.adapters/db->user-command-history user)))
