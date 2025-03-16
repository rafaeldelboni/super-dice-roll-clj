(ns super-dice-roll.slack.ports.http-in
  (:require [schema.core :as s]
            [super-dice-roll.adapters :as base.adapters]
            [super-dice-roll.controllers :as base.controller]
            [super-dice-roll.messages :as messages]
            [super-dice-roll.schemas.types :as schemas.types]
            [super-dice-roll.slack.adapters :as slack.adapters]
            [super-dice-roll.slack.schemas.http-in :as slack.schemas.http-in]))

(s/defn ^:private command->message
  [{:keys [command] :as body} :- slack.schemas.http-in/Command
   components :- schemas.types/Components]
  (case command
    "/roll"    (let [roll-cmd (slack.adapters/wire-in->model body)
                     rolled (base.controller/do-roll! roll-cmd components)]
                 (if rolled
                   (base.adapters/rolled->message rolled)
                   (base.adapters/roll-command->error-message roll-cmd)))
    "/history" (-> (slack.adapters/wire-in->user body)
                   (base.controller/get-user-command-history components)
                   base.adapters/user-command-history->message)
    "/help"    (messages/help :slack)
    nil))

(defn process-command!
  [{{body :body form :form} :parameters
    components :components}]
  (let [payload (or form body)
        message (command->message payload components)]
    {:status 200
     :body (if message
             {:response_type "in_channel"
              :text message}
             {})}))

(defn process-oauth!
  [_]
  {:status 200
   :body "<html><body><h1>Success!</h1></body></html>"})
