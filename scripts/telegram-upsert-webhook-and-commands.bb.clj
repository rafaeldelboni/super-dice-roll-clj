#!/usr/bin/env bb

(require '[babashka.curl :as curl]
         '[cheshire.core :as json]
         '[clojure.string :as string])

(def bot-token (or (System/getenv "TELEGRAM_BOT_TOKEN") "<BOT-TOKEN>"))
(def bot-webhook-url (or (System/getenv "TELEGRAM_BOT_WEBHOOK") "<BOT-WEBHOOK>"))
(defn build-commands-url
  [method]
  (str "https://api.telegram.org/bot" bot-token "/" method))

(def commands
  (json/encode
   {:commands [{:command "roll"
                :description "Roll dices"}
               {:command "history"
                :description "Lists your lasts 10 rolls with the results."}
               {:command "help"
                :description "Get help about using this bot `help`."}]}))

(defn info-webhook-url []
  (-> (build-commands-url "getWebhookInfo")
      (curl/get {:headers {"Accept" "application/json"
                           "Content-Type" "application/json"}
                 :throw true})
      (dissoc :headers)
      println))

(defn set-webhook-url []
  (-> (build-commands-url "setWebhook")
      (curl/post {:headers {"Accept" "application/json"
                            "Content-Type" "application/json"}
                  :body (json/encode {:url (str bot-webhook-url "/" bot-token)})
                  :throw true})
      (dissoc :headers)
      println))

(defn upsert-commands []
  (-> (build-commands-url "setMyCommands")
      (curl/post {:headers {"Accept" "application/json"
                            "Content-Type" "application/json"}
                  :body commands
                  :throw true})
      (dissoc :headers)
      println))

(defn list-current-commands []
  (-> (build-commands-url "getMyCommands")
      (curl/post {:headers {"Accept" "application/json"
                            "Content-Type" "application/json"}
                  :throw false})
      :body
      (json/decode true)
      println))

(let [args *command-line-args*]
  (when (string/includes? args "--info-webhook")
    (info-webhook-url))
  (when (string/includes? args "--update-webhook")
    (set-webhook-url))
  (when (string/includes? args "--update-cmd")
    (upsert-commands))
  (when (string/includes? args "--list-cmd")
    (list-current-commands)))
