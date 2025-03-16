(ns integration.super-dice-roll.slack.oauth-test
  (:require [com.stuartsierra.component :as component]
            [integration.super-dice-roll.util :as util]
            [matcher-combinators.matchers :as matchers]
            [parenthesin.components.config.aero :as components.config]
            [parenthesin.components.db.jdbc-hikari :as components.database]
            [parenthesin.components.http.clj-http :as components.http]
            [parenthesin.components.server.reitit-pedestal-jetty :as components.webserver]
            [parenthesin.helpers.state-flow.http :as state-flow.http]
            [state-flow.api :refer [defflow]]
            [state-flow.assertions.matcher-combinators :refer [match?]]
            [state-flow.core :refer [flow]]
            [super-dice-roll.components.router :as components.router]
            [super-dice-roll.routes :as routes]))

(def client-id "2141029472.691202649728")
(def client-secret "e1b9e11dfcd19c1982d5de12921e17e8c")
(def code "4724469134.4644010092847.232b4e6d82c333b475fc30f5f5a341d294feb1a94392c2fd791f7ab7731a443d1a")

(defn- create-and-start-components! []
  (component/start-system
   (component/system-map
    :config (components.config/new-config
             {:slack {:client-id client-id
                      :client-secret client-secret}})
    :http (components.http/new-http-mock
           {"https://slack.com/api/oauth.v2.access"
            {:status 200
             :body {:ok true}}})
    :router (components.router/new-router routes/routes)
    :database (component/using (components.database/new-database)
                               [:config])
    :webserver (component/using (components.webserver/new-webserver)
                                [:config :http :router :database]))))

(defflow
  flow-integration-slack-oauth-test
  {:init (util/start-system! create-and-start-components!)
   :cleanup util/stop-system!
   :fail-fast? true}
  (flow "should interact with system"

    (flow "should not error on http out"
      (match? (matchers/embeds {:status 200
                                :body "<html><body><h1>Success!</h1></body></html>"})
              (util/request! {:method :get
                              :uri    (str "/slack/oauth?code=" code)})))

    (flow "should send payload to slack api with client credentials"
      (match? {:client_id client-id
               :client_secret client-secret
               :code code}
              (state-flow.http/http-out-requests
               (fn [requests]
                 (->> requests
                      (filter #(= (:url %) "https://slack.com/api/oauth.v2.access"))
                      first
                      :form-params)))))

    (state-flow.http/set-http-out-responses! {"https://slack.com/api/oauth.v2.access"
                                              {:status 200
                                               :body {:ok false
                                                      :error "out of inspiration error"}}})

    (flow "should error on http out"
      (match? (matchers/embeds {:status 200
                                :body "<html><body><h1>Error!</h1><pre>out of inspiration error</pre></body></html>"})
              (util/request! {:method :get
                              :uri    (str "/slack/oauth?code=" code)})))))
