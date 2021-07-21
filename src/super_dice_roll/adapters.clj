(ns super-dice-roll.adapters
  (:require [cheshire.core :as json]
            [schema.core :as s]
            [super-dice-roll.schemas.db :as schemas.db]
            [super-dice-roll.schemas.models :as schemas.models]))

(s/defn rolled->db-new-roll :- schemas.db/NewRoll
  [{:keys [roll results]} :- schemas.models/Rolled]
  (let [{:keys [id channel]} (get-in roll [:command :user])
        {:keys [command modifier]} roll
        {:keys [each total]} results]
    {:rolls/user_id id
     :rolls/channel_id (channel schemas.models/ChannelDefinition)
     :rolls/command (:command command)
     :rolls/modifier modifier
     :rolls/total total
     :rolls/each (json/encode each)}))

(s/defn db->user-command-history :- schemas.models/UserCommandHistory
  [rolls :- [schemas.db/Roll]
   user :- schemas.models/User]
  {:user user
   :history (mapv (fn [{:rolls/keys [command each modifier total]}]
                    {:command command
                     :results {:total total
                               :modifier modifier
                               :each (-> each (json/decode true) vec)}})
                  rolls)})
