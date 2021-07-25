(ns super-dice-roll.telegram.adapters
  (:require [clojure.string :as string]
            [schema.core :as s]
            [super-dice-roll.schemas.models :as schemas.models]
            [super-dice-roll.telegram.schemas.http-in :as telegram.schemas.http-in]))

(s/defn wire-in->user :- schemas.models/User
  [{:keys [message]} :- telegram.schemas.http-in/Update]
  (let [{{:keys [id username]} :from} message]
    {:id (str id)
     :username (str username)
     :nick ""
     :channel :telegram}))

(s/defn wire-in->model :- schemas.models/RollCommand
  [{:keys [message] :as update-message} :- telegram.schemas.http-in/Update]
  (let [command (or (-> (:text message) (string/split #"/roll") last) "")]
    {:user (wire-in->user update-message)
     :command (string/trim command)}))
