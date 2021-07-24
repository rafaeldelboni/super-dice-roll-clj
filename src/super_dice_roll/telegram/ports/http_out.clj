(ns super-dice-roll.telegram.ports.http-out
  (:require [parenthesin.components.http :as components.http]
            [parenthesin.logs :as logs]
            [schema.core :as s]
            [super-dice-roll.schemas.types :as schemas.types]))

(s/defn send-message
  [message :- s/Str
   message-id :- s/Int
   {:keys [config http]} :- schemas.types/Components]
  (let [bot-token (get-in config [:config :telegram :bot-token])]
    (try 
    (components.http/request
     http
     {:url (str "https://api.telegram.org/bot" bot-token "/sendMessage")
      :query-params {:chat_id message-id
                     :text message
                     :parse_mode "HTML"}
      :accept :json
      :method :get})
    (catch Exception e
      logs/log :error :http-out-message-response e))))
