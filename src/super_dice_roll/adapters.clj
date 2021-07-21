(ns super-dice-roll.adapters
  (:require [cheshire.core :as json]
            [schema.core :as s]
            [super-dice-roll.schemas.db :as schemas.db]
            [super-dice-roll.schemas.models :as schemas.models]))

(s/defn rolled->db-new-roll :- schemas.db/NewRoll
  [{:keys [roll results]} :- schemas.models/Rolled]
  (let [{:keys [id channel]} (get-in roll [:command :user])
        command (get-in roll [:command :command])
        {:keys [each total]} results]
    {:rolls/user_id id
     :rolls/channel_id (channel schemas.models/ChannelDefinition)
     :rolls/command command
     :rolls/total total
     :rolls/each (json/encode each)}))

(s/defn db->user-command-history :- schemas.models/UserCommandHistory
  [rolls :- [schemas.db/Roll]
   user :- schemas.models/User]
  {:user user
   :history (mapv (fn [{:rolls/keys [command total each]}]
                    {:command command
                     :results {:total total
                               :each (-> each (json/decode true) vec)}})
                  rolls)})
