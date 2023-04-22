(ns integration.super-dice-roll.discord.commands-test
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
            [super-dice-roll.components.router :as components.router]
            [super-dice-roll.discord.security :as discord.security]
            [super-dice-roll.messages :as messages]
            [super-dice-roll.routes :as routes]))

(clojure.test/use-fixtures :once schema.test/validate-schemas)

(defn- create-and-start-components! []
  (let [key-pair (discord.security/generate-keypair)]
    (component/start-system
     (component/system-map
      :config (components.config/new-config
               {:discord {:app-public-key (discord.security/bytes->hex (.getEncoded (:public key-pair)))
                          :app-test-signer (discord.security/new-signer (:private key-pair))}})
      :http (components.http/new-http-mock {})
      :router (components.router/new-router routes/routes)
      :database (component/using (components.database/new-database)
                                 [:config])
      :webserver (component/using (components.webserver/new-webserver)
                                  [:config :http :router :database])))))

(defflow
  flow-integration-wallet-test
  {:init (util/start-system! create-and-start-components!)
   :cleanup util/stop-system!
   :fail-fast? true}
  (flow "should interact with system"

    (flow "should be able to send a /roll command"
      (match? (matchers/embeds {:status 200
                                :body  {:type 4
                                        :data {:content (matchers/regex #"papoulos rolled 4d6-1")}}})
              (util/signed-request! {:method :post
                                     :uri    "/discord/webhook"
                                     :body   {:id "discord-id-1"
                                              :application_id "discord-app-id-1"
                                              :token "discord-token-1"
                                              :version 1
                                              :type 2
                                              :member {:user {:id "discord-user-id-1"
                                                              :username "papoulos"}}
                                              :data {:id "discord-data-id-1"
                                                     :type 1
                                                     :name "roll"
                                                     :options [{:id "discord-data-options-1"
                                                                :name "command"
                                                                :type 3
                                                                :value "4d6-1"}]}}})))

    (flow "should be able to send a /history command"
      (match? (matchers/embeds {:status 200
                                :body  {:type 4
                                        :data {:content (matchers/regex #"\*papoulos history\*\n`4d6-1: \[")}}})
              (util/signed-request! {:method :post
                                     :uri    "/discord/webhook"
                                     :body   {:id "discord-id-1"
                                              :application_id "discord-app-id-1"
                                              :token "discord-token-1"
                                              :version 1
                                              :type 2
                                              :member {:user {:id "discord-user-id-1"
                                                              :username "papoulos"}}
                                              :data {:id "discord-data-id-1"
                                                     :type 1
                                                     :name "history"}}})
              {:times-to-try 5
               :sleep-time   200}))

    (flow "should be able to send a /help command"
      (match? (matchers/embeds {:status 200
                                :body  {:type 4
                                        :data {:content (messages/help :discord)}}})
              (util/signed-request! {:method :post
                                     :uri    "/discord/webhook"
                                     :body   {:id "discord-id-1"
                                              :application_id "discord-app-id-1"
                                              :token "discord-token-1"
                                              :version 1
                                              :type 2
                                              :member {:user {:id "discord-user-id-1"
                                                              :username "papoulos"}}
                                              :data {:id "discord-data-id-1"
                                                     :type 1
                                                     :name "help"}}})))))
