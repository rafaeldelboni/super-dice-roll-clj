(ns integration.super-dice-roll.slack.security-test
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

(def body-payload {:token "gIkuvaNzQIHg97ATvDxqgjtO"
                   :team_id "T0001"
                   :team_domain "example"
                   :enterprise_id "E0001"
                   :enterprise_name "Globular%20Construct%20Inc"
                   :channel_id "C2147483705"
                   :channel_name "test"
                   :user_id "U2147483697"
                   :user_name "Steve"
                   :command "/help"
                   :text ""
                   :response_url "https://hooks.slack.com/commands/1234/5678"
                   :trigger_id "13345224609.738474920.8088930838d88f008e0"
                   :api_app_id "A123456"})

(defflow
  flow-integration-wallet-test
  {:init (util/start-system! create-and-start-components!)
   :cleanup util/stop-system!
   :fail-fast? true}
  (flow "should interact with system as slack"
    (flow "should be able to send a signed request"
      (match? (matchers/embeds {:status 200
                                :body  {:response_type "in_channel"
                                        :text "Available commands: `/roll`, `/history` or `/help`\n\n`/roll <NDM>`\nYou must specify dice and modifiers in following format:\nN = Number of dices\nD = Dice type (D6, D12, D20)\nM = Modifiers (+1, -3)\nExample: `/roll 3D6+3`\n\n`/history`\nLists your lasts 10 rolls with the results.\n\n<https://j.mp/sdr-bot|Source Code>\n"}})
              (util/slack-signed-request! {:method :post
                                           :uri    "/slack/slash/help"
                                           :body   body-payload})))

    (flow "should not be able to send a unsigned request slack"
      (match? (matchers/embeds {:status 401
                                :body  "invalid request signature"})
              (util/request! {:method :post
                              :uri    "/slack/slash/roll"
                              :headers {"x-slack-signature" "x"
                                        "x-slack-request-timestamp" "x"}
                              :body   body-payload})))))
