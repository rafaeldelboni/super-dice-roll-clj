(ns integration.super-dice-roll.slack.commands-test
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
            [super-dice-roll.messages :as messages]
            [super-dice-roll.routes :as routes]))

(clojure.test/use-fixtures :once schema.test/validate-schemas)

(defn- create-and-start-components! []
  (component/start-system
   (component/system-map
    :config (components.config/new-config
             {:slack {:signing-secret "8f742231b10e8888abcd99yyyzzz85a5"}})
    :http (components.http/new-http-mock {})
    :router (components.router/new-router routes/routes)
    :database (component/using (components.database/new-database)
                               [:config])
    :webserver (component/using (components.webserver/new-webserver)
                                [:config :http :router :database]))))

(defflow
  flow-integration-wallet-test
  {:init (util/start-system! create-and-start-components!)
   :cleanup util/stop-system!
   :fail-fast? true}
  (flow "should interact with system"

    (flow "should be able to send a /roll command"
      (match? (matchers/embeds {:status 200
                                :body  {:response_type "in_channel"
                                        :text (matchers/regex #"papoulos rolled 4d6-1")}})
              (util/slack-signed-request! {:method :post
                                           :uri    "/slack/slash/roll"
                                           :body   {:user_id "U2147483697"
                                                    :user_name "papoulos"
                                                    :command "/roll"
                                                    :text "4d6-1"}})))

    (flow "should be able to send a /history command"
      (match? (matchers/embeds {:status 200
                                :body  {:response_type "in_channel"
                                        :text (matchers/regex #"\*papoulos history\*\n`4d6-1: \[")}})
              (util/slack-signed-request! {:method :post
                                           :uri    "/slack/slash/history"
                                           :body   {:user_id "U2147483697"
                                                    :user_name "papoulos"
                                                    :command "/history"
                                                    :text ""}})
              {:times-to-try 5
               :sleep-time   200}))

    (flow "should be able to send a /help command"
      (match? (matchers/embeds {:status 200
                                :body  {:response_type "in_channel"
                                        :text (messages/help :slack)}})
              (util/slack-signed-request! {:method :post
                                           :uri    "/slack/slash/help"
                                           :body   {:user_id "U2147483697"
                                                    :user_name "papoulos"
                                                    :command "/help"
                                                    :text ""}})))))
