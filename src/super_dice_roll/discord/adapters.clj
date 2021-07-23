(ns super-dice-roll.discord.adapters
  (:require [clojure.string :as string]
            [schema.core :as s]
            [super-dice-roll.adapters :as adapters]
            [super-dice-roll.discord.schemas.http-in :as discord.schemas.http-in]
            [super-dice-roll.messages :as messages]
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

(s/defn rolled->message :- s/Str
  [{:keys [roll results]} :- schemas.models/Rolled]
  (let [{:keys [each modifier total]} results
        {:keys [nick username]} (get-in roll [:command :user])
        command (get-in roll [:command :command])]
    (str "*" (adapters/->name username nick) " rolled " command "*\n"
         "`[" (string/join "," each) "]" (adapters/->modifier modifier) "`\n"
         "**total: " total "**\n")))

(s/defn roll-command->error-message :- s/Str
  [{:keys [user command]} :- schemas.models/RollCommand]
  (let [{:keys [nick username]} user]
    (str (adapters/->name username nick) " the command *" command "* is invalid\n"
         messages/help-roll)))

(s/defn ^:private roll-command-result->message :- s/Str
  [{:keys [command results]} :- schemas.models/RollCommandResults]
  (let [{:keys [each modifier total]} results]
    (str "`" command ": " "[" (string/join "," each) "]"
         (adapters/->modifier modifier)
         " = " total "`\n")))

(s/defn user-command-history->message :- s/Str
  [{:keys [user history]} :- schemas.models/UserCommandHistory]
  (let [{:keys [nick username]} user]
    (str "*" (adapters/->name username nick) " history*\n"
         (apply str (mapv #(roll-command-result->message %) history)))))
