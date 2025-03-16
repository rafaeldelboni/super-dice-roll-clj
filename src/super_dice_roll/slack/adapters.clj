(ns super-dice-roll.slack.adapters
  (:require [clojure.string :as string]
            [schema.core :as s]
            [super-dice-roll.schemas.models :as schemas.models]
            [super-dice-roll.slack.schemas.http-in :as slack.schemas.http-in]))

(s/defn wire-in->user :- schemas.models/User
  [{:keys [user_id user_name]} :- slack.schemas.http-in/Command]
  {:id (str user_id)
   :username (str user_name)
   :nick ""
   :channel :slack})

(s/defn wire-in->model :- schemas.models/RollCommand
  [{:keys [text] :as input} :- slack.schemas.http-in/Command]
  (let [command (if (not (string/blank? text)) (string/trim text) "")]
    {:user (wire-in->user input)
     :command command}))
