(ns super-dice-roll.slack.interceptor
  (:require [super-dice-roll.slack.security :as slack.security]))

(defn now-secs [] (quot (System/currentTimeMillis) 1000))

(defn authentication-interceptor []
  {:name ::validate-request-interaction
   :enter (fn [ctx]
            (let [request (:request ctx)
                  {{:keys [config]} :components} request
                  now (now-secs)
                  signing-secret (get-in config [:config :slack :signing-secret])
                  signature (get-in ctx [:request :headers "x-slack-signature"])
                  timestamp (get-in ctx [:request :headers "x-slack-request-timestamp"])
                  raw-body (get ctx :raw-body)]
              (if (slack.security/verify-request signing-secret now timestamp raw-body signature)
                ctx
                (assoc ctx :response {:headers {"Content-Type" "application/text"}
                                      :status 401
                                      :body "invalid request signature"}))))})
