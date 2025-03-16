(ns integration.super-dice-roll.util
  (:require [cheshire.core :as json]
            [clojure.string :as string]
            [com.stuartsierra.component :as component]
            [parenthesin.helpers.logs :as logs]
            [parenthesin.helpers.migrations :as migrations]
            [parenthesin.helpers.state-flow.server.pedestal :as state-flow.server]
            [pg-embedded-clj.core :as pg-emb]
            [ring.util.codec :as codec]
            [state-flow.api :as state-flow.api]
            [state-flow.core :as state-flow :refer [flow]]
            [super-dice-roll.adapters.bytes :as adapters.bytes]
            [super-dice-roll.discord.security :as discord.security]
            [super-dice-roll.slack.security :as slack.security]))

(defn request! [args] (state-flow.server/request! args))

(defn discord-signed-request!
  [{:keys [body] :as request}]
  (flow "makes discord signed http request"
    [signer (state-flow.api/get-state (comp :app-test-signer :discord :config :config))
     :let [timestamp (str (quot (System/currentTimeMillis) 1000))
           signature (->> (str timestamp (json/encode body))
                          .getBytes
                          (discord.security/sign signer)
                          adapters.bytes/bytes->hex)]]
    (request! (-> request
                  (assoc-in [:headers "x-signature-ed25519"] signature)
                  (assoc-in [:headers "x-signature-timestamp"] timestamp)))))

(defn slack-signed-request!
  [{:keys [body] :as request}]
  (flow "makes discord signed http request"
    [signing-secret (state-flow.api/get-state (comp :signing-secret :slack :config :config))
     :let [timestamp (str (quot (System/currentTimeMillis) 1000))
           basestring (slack.security/hash-hmac-sha256
                       (string/join ":" [slack.security/version timestamp (codec/form-encode body)])
                       signing-secret)
           signature (str slack.security/version "=" (adapters.bytes/bytes->hex basestring))]]
    (request! (-> request
                  (assoc-in [:headers "Content-Type"] "application/x-www-form-urlencoded")
                  (assoc-in [:headers "x-slack-signature"] signature)
                  (assoc-in [:headers "x-slack-request-timestamp"] timestamp)))))

(defn start-system!
  [system-start-fn]
  (fn []
    (logs/setup :info :auto)
    (pg-emb/init-pg)
    (migrations/migrate (migrations/configuration-with-db))
    (system-start-fn)))

(defn stop-system!
  [system]
  (component/stop-system system)
  (pg-emb/halt-pg!))
