(ns super-dice-roll.slack.ports.http-out
  (:require [parenthesin.components.http.clj-http :as components.http]
            [parenthesin.helpers.logs :as logs]
            [schema.core :as s]
            [super-dice-roll.schemas.types :as schemas.types]))

(s/defn exchange-token
  [code :- s/Str
   {:keys [config http]} :- schemas.types/Components]
  (let [client-id (get-in config [:config :slack :client-id])
        client-secret (get-in config [:config :slack :client-secret])]
    (try
      (components.http/request
       http
       {:url "https://slack.com/api/oauth.v2.access"
        :form-params {:client_id client-id
                      :client_secret client-secret
                      :code code}
        :method :post
        :as :json})
      (catch Exception e
        (logs/log :error :http-out-message-response e)
        {:body {:ok false :error (ex-message e)}}))))
