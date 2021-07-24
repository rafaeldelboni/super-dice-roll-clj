(ns super-dice-roll.discord.adapters
  (:require [schema.core :as s]
            [super-dice-roll.discord.schemas.http-in :as discord.schemas.http-in]
            [super-dice-roll.schemas.models :as schemas.models]))

(s/defn wire-in->user :- schemas.models/User
  [{{:keys [user nick]} :member} :- discord.schemas.http-in/InteractionRequest]
  (let [{:keys [id username]} user]
    {:id id
     :username username
     :nick (str nick)
     :channel :discord}))

(s/defn wire-in->model :- schemas.models/RollCommand
  [{{:keys [options]} :data :as interaction} :- discord.schemas.http-in/InteractionRequest]
  {:user (wire-in->user interaction)
   :command (-> options first :value)})
