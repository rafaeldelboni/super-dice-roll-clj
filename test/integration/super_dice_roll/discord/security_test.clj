(ns integration.super-dice-roll.discord.security-test
  (:require [clojure.test :as clojure.test]
            [com.stuartsierra.component :as component]
            [integration.super-dice-roll.util :as util]
            [matcher-combinators.matchers :as matchers]
            [parenthesin.components.config.aero :as components.config]
            [parenthesin.components.db.jdbc-hikari :as components.database]
            [parenthesin.components.http.clj-http :as components.http]
            [parenthesin.components.server.reitit-pedestal-jetty :as components.webserver]
            [schema.test :as schema.test]
            [state-flow.api :refer [defflow]]
            [state-flow.assertions.matcher-combinators :refer [match?]]
            [state-flow.core :as state-flow :refer [flow]]
            [super-dice-roll.adapters.bytes :as adapters.bytes]
            [super-dice-roll.components.router :as components.router]
            [super-dice-roll.discord.security :as discord.security]
            [super-dice-roll.routes :as routes]))

(clojure.test/use-fixtures :once schema.test/validate-schemas)

(defn- create-and-start-components! []
  (let [key-pair (discord.security/generate-keypair)]
    (component/start-system
     (component/system-map
      :config (components.config/new-config
               {:discord {:app-public-key (adapters.bytes/bytes->hex (.getEncoded (:public key-pair)))
                          :app-test-signer (discord.security/new-signer (:private key-pair))}})
      :http (components.http/new-http-mock {})
      :router (components.router/new-router routes/routes)
      :database (component/using (components.database/new-database)
                                 [:config])
      :webserver (component/using (components.webserver/new-webserver)
                                  [:config :http :router :database])))))

(defflow
  flow-integration-discord-security-test
  {:init (util/start-system! create-and-start-components!)
   :cleanup util/stop-system!
   :fail-fast? true}
  (flow "should interact with system as discord"
    (flow "should be able to send a signed request"
      (match? (matchers/embeds {:status 200
                                :body  {:type 1}})
              (util/discord-signed-request! {:method :post
                                             :uri    "/discord/webhook"
                                             :body   {:id "discord-id-1"
                                                      :application_id "discord-app-id-1"
                                                      :type 1
                                                      :token "discord-token-1"
                                                      :version 1}})))

    (flow "should not be able to send a unsigned request discord"
      (match? (matchers/embeds {:status 401
                                :body  "invalid request signature"})
              (util/request! {:method :post
                              :uri    "/discord/webhook"
                              :headers {"x-signature-ed25519" "x"
                                        "x-signature-timestamp" "x"}
                              :body   {:id "discord-id-2"
                                       :application_id "discord-app-id-2"
                                       :type 1
                                       :token "discord-token-2"
                                       :version 1}})))))
