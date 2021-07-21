(ns super-dice-roll.schemas.db
  (:require [schema.core :as s]
            [super-dice-roll.schemas.types :as schemas.types]))

(def roll-skeleton {:rolls/id s/Int
                    :rolls/user_id s/Str
                    :rolls/channel_id s/Int
                    :rolls/command s/Str
                    :rolls/modifier s/Int
                    :rolls/total s/Int
                    :rolls/each schemas.types/JsonArrayInt
                    :rolls/created_at s/Inst})

(s/defschema NewRoll
  (select-keys roll-skeleton  [:rolls/user_id
                               :rolls/channel_id
                               :rolls/command
                               :rolls/modifier
                               :rolls/total
                               :rolls/each]))

(s/defschema Roll roll-skeleton)
