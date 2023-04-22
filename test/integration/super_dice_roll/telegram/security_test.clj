(ns integration.super-dice-roll.telegram.security-test
  (:require [clojure.test :as clojure.test]
            [com.stuartsierra.component :as component]
            [integration.super-dice-roll.util :as util]
            [matcher-combinators.matchers :as matchers]
            [parenthesin.components.config.aero :as components.config]
            [parenthesin.components.db.jdbc-hikari :as components.database]
            [parenthesin.components.http.clj-http :as components.http]
            [parenthesin.components.server.reitit-pedestal-jetty :as components.webserver]
            [parenthesin.helpers.state-flow.server.pedestal :as state-flow.server]
            [schema.test :as schema.test]
            [state-flow.api :refer [defflow]]
            [state-flow.assertions.matcher-combinators :refer [match?]]
            [state-flow.core :as state-flow :refer [flow]]
            [super-dice-roll.components.router :as components.router]
            [super-dice-roll.routes :as routes]))

(clojure.test/use-fixtures :once schema.test/validate-schemas)

(def telegram-bot-token "123456:ABC-DEF1234ghIkl-zyx57W2v1u123ew11")

(defn- create-and-start-components! []
  (component/start-system
   (component/system-map
    :config (components.config/new-config
             {:telegram {:bot-token telegram-bot-token}})
    :http (components.http/new-http-mock
           {(str "https://api.telegram.org/bot" telegram-bot-token "/sendMessage")
            {:status 200}})
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
    [:let [request-body {:update_id 987654321
                         :message {:date 1626904234
                                   :entities [{:offset 0
                                               :type "bot_command"
                                               :length 5}]
                                   :chat {:first_name "Schua"
                                          :username "schuazeneger"
                                          :type "private"
                                          :id 111
                                          :last_name "Zeneger"}
                                   :message_id 1
                                   :from {:first_name "Schua"
                                          :language_code "en"
                                          :is_bot false
                                          :username "schuazeneger"
                                          :id 12345678
                                          :last_name "Zeneger"}
                                   :text "/roll 4d6-1"}}]]

    (flow "should be able to send a signed request"
      (match? (matchers/embeds {:status 200})
              (state-flow.server/request! {:method :post
                                        :uri    (str "/telegram/webhook/" telegram-bot-token)
                                        :body   request-body})))

    (flow "should not be able to send a unsigned request"
      (match? (matchers/embeds {:status 401
                                :body  "invalid bot-token sent"})
              (state-flow.server/request! {:method :post
                                        :uri    "/telegram/webhook/very-wrong-token"
                                        :body   request-body})))))
