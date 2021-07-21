(ns super-dice-roll.discord.ports.http-in
  (:require [parenthesin.logs :as logs]
            [schema.core :as s]
            [super-dice-roll.controllers :as base.controller]
            [super-dice-roll.discord.adapters :as discord.adapters]
            [super-dice-roll.discord.schemas.http-in :as discord.schemas.http-in]
            [super-dice-roll.messages :as messages]
            [super-dice-roll.schemas.types :as schemas.types]))

(s/defn application-command-handler!
  [body :- discord.schemas.http-in/InteractionRequest
   components :- schemas.types/Components]
  (let [slash-cmd (get-in body [:data :name])
        content (case slash-cmd
                  "roll" (let [roll-cmd (discord.adapters/wire-in->model body)
                               rolled (base.controller/do-roll! roll-cmd components)]
                           (if rolled
                             (discord.adapters/rolled->message rolled)
                             (discord.adapters/roll-command->error-message roll-cmd)))
                  "history" (-> (discord.adapters/wire-in->user body)
                                (base.controller/get-user-command-history components)
                                discord.adapters/user-command-history->message)
                  "help" (str messages/help-header "\n"
                              messages/help-roll "\n"
                              messages/help-history))]
    {:status 200
     :body {:type 4
            :data {:content content}}}))

(defn process-interaction!
  [{{{:keys [type] :as body} :body
     headers :header} :parameters
    components :components}]
  (logs/log :info {:header headers :body body})
  (case type
    1 {:status 200
       :body {:type 1}}
    2 (application-command-handler! body components)
    {:status 200
     :body {:type 4
            :data {:content (str "Unknown command. " messages/help-header)}}}))
