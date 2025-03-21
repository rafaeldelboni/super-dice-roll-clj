(ns super-dice-roll.routes
  (:require [reitit.swagger :as swagger]
            [schema.core :as s]
            [super-dice-roll.discord.interceptor :as discord.interceptor]
            [super-dice-roll.discord.ports.http-in :as discord.ports.http-in]
            [super-dice-roll.discord.schemas.http-in :as discord.schemas.http-in]
            [super-dice-roll.slack.interceptor :as slack.interceptor]
            [super-dice-roll.slack.ports.http-in :as slack.ports.http-in]
            [super-dice-roll.slack.schemas.http-in :as slack.schemas.http-in]
            [super-dice-roll.telegram.interceptor :as telegram.interceptor]
            [super-dice-roll.telegram.ports.http-in :as telegram.ports.http-in]
            [super-dice-roll.telegram.schemas.http-in :as telegram.schemas.http-in]))

(def routes
  [["/swagger.json"
    {:get {:no-doc true
           :swagger {:info {:title "super-dice-roll"
                            :description "Bot to roll dices."}}
           :handler (swagger/create-swagger-handler)}}]

   ["/discord"
    {:swagger {:tags ["discord"]}
     :interceptors [(discord.interceptor/authentication-interceptor)]
     :parameters {:header {:x-signature-ed25519 s/Str
                           :x-signature-timestamp s/Str}}}

    ["/webhook"
     {:post {:summary "Discord webhook-based interactions."
             :parameters {:body discord.schemas.http-in/InteractionRequest}
             :responses {200 {:body discord.schemas.http-in/InteractionResponse}
                         400 {:body s/Str}
                         401 {:body s/Str}
                         500 {:body s/Str}}
             :handler discord.ports.http-in/process-interaction!}}]]

   ["/slack"
    {:swagger {:tags ["slack"]}}

    ["/slash/:command"
     {:interceptors [(slack.interceptor/authentication-interceptor)]
      :post {:summary "Slack we'll send a payload to when the command is invoked."
             :parameters {:header {:x-slack-signature s/Str
                                   :x-slack-request-timestamp s/Str}
                          :path {:command s/Str}
                          :form (s/maybe slack.schemas.http-in/Command)
                          :body (s/maybe slack.schemas.http-in/Command)}
             :responses {200 {:body s/Any}
                         400 {:body s/Str}
                         401 {:body s/Str}
                         500 {:body s/Str}}
             :handler slack.ports.http-in/process-command!}}]

    ["/oauth"
     {:get {:summary "Slack oauth redirect url"
            :parameters {:query {:code s/Str}}
            :responses {200 {:body s/Any}
                        400 {:body s/Str}
                        401 {:body s/Str}
                        500 {:body s/Str}}
            :handler slack.ports.http-in/process-oauth!}}]]

   ["/telegram"
    {:swagger {:tags ["telegram"]}
     :interceptors [(telegram.interceptor/verification-interceptor)]}

    ["/webhook/:bot-token"
     {:post {:summary "Telegram webhook-based interactions."
             :parameters {:path {:bot-token s/Str}
                          :body telegram.schemas.http-in/Update}
             :responses {200 {:body s/Any}
                         400 {:body s/Str}
                         401 {:body s/Str}
                         500 {:body s/Str}}
             :handler telegram.ports.http-in/process-update!}}]]])
