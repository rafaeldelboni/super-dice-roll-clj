(ns super-dice-roll.telegram.ports.http-out
  (:require [parenthesin.components.http :as components.http]
            [schema.core :as s]
            [super-dice-roll.schemas.types :as schemas.types]))

(s/defn send-message
  [message :- s/Str
   message-id :- s/Str
   {:keys [config http]} :- schemas.types/Components]
  (let [bot-token (get-in config [:config :telegram :bot-token])]
    (components.http/request
     http
     {:url (str "https://api.telegram.org/bot" bot-token "/sendMessage")
      :query-params {:chat_id message-id
                     :text message
                     :parse_mode "HTML"}
      :accept :json
      :method :get})))
