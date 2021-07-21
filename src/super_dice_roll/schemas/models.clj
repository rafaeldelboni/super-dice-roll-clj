(ns super-dice-roll.schemas.models
  (:require [schema.core :as s]))

(def ChannelDefinition
  {:discord  1
   :telegram 2})
(def Channel (apply s/enum (keys ChannelDefinition)))

(s/defschema User
  {:id s/Str
   :username s/Str
   :nick s/Str
   :channel Channel})

(s/defschema RollCommand
  {:user User
   :command s/Str})

(s/defschema Roll
  {:command RollCommand
   :times s/Int
   :dice s/Int
   :modifier s/Int})

(s/defschema EachResult [s/Int])

(s/defschema Results
  {:each EachResult
   :total s/Int})

(s/defschema Rolled
  {:roll Roll
   :results Results})

(s/defschema RollCommandResults
  {:command s/Str
   :results Results})

(s/defschema UserCommandHistory
  {:user User
   :history [RollCommandResults]})
