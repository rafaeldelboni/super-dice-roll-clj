#!/usr/bin/env bb

(require '[babashka.curl :as curl]
         '[cheshire.core :as json]
         '[clojure.string :as string])

(def bot-token (or (System/getenv "DISCORD_BOT_TOKEN") "<BOT-TOKEN>"))
(def application-id (or (System/getenv "DISCORD_APP_ID") "<APP-ID>"))
(def commands-url (str "https://discord.com/api/v8/applications/" application-id "/commands"))

(def commands
  (json/encode
   [{:name "roll"
     :description "Roll dices"
     :options [{:name "command"
                :description "<NDM> N = Number of dices D = Type of dices (D6, D12, D20) M = Modifiers (+1, -3), Example: 3D6+3"
                :type 3
                :required true}]}
    {:name "history"
     :description "Lists your lasts 10 rolls with the results."
     :type 1}
    {:name "help"
     :description "Get help about using this bot `help`."
     :type 1}]))

(defn upsert-commands []
  (-> commands-url
      (curl/put {:headers {"Accept" "application/json"
                           "Content-Type" "application/json"
                           "Authorization" (str "Bot " bot-token)}
                 :body commands
                 :throw true})
      (dissoc :headers)
      println))

(defn list-current-commands []
  (-> commands-url
      (curl/get {:headers {"Accept" "application/json"
                           "Content-Type" "application/json"
                           "Authorization" (str "Bot " bot-token)}
                 :throw false})
      :body
      (json/decode true)
      println))

(let [args *command-line-args*]
  (when (string/includes? args "--update")
    (upsert-commands))
  (when (string/includes? args "--list")
    (list-current-commands)))
