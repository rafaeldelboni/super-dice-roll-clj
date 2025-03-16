(ns super-dice-roll.adapters
  (:require [cheshire.core :as json]
            [clojure.string :as string]
            [schema.core :as s]
            [selmer.parser :as selmer]
            [super-dice-roll.messages :as messages]
            [super-dice-roll.schemas.db :as schemas.db]
            [super-dice-roll.schemas.models :as schemas.models]))

(s/defn rolled->db-new-roll :- schemas.db/NewRoll
  [{:keys [roll results]} :- schemas.models/Rolled]
  (let [{:keys [id channel]} (get-in roll [:command :user])
        {:keys [command modifier]} roll
        {:keys [each total]} results]
    {:rolls/user_id id
     :rolls/channel_id (channel schemas.models/ChannelDefinition)
     :rolls/command (:command command)
     :rolls/modifier modifier
     :rolls/total total
     :rolls/each (json/encode each)}))

(s/defn db->user-command-history :- schemas.models/UserCommandHistory
  [rolls :- [schemas.db/Roll]
   user :- schemas.models/User]
  (let [roll-count (count rolls)]
    {:user user
     :history (if (> roll-count 0)
                (mapv (fn [{:rolls/keys [command each modifier total]}]
                        {:command command
                         :results {:total total
                                   :modifier modifier
                                   :each (-> each (json/decode true) vec)}})
                      rolls)
                [])}))

(s/defn ->name :- s/Str
  [username :- s/Str
   nick :- s/Str]
  (if (empty? nick)
    username
    nick))

(s/defn ->modifier :- s/Str
  [modifier :- s/Int]
  (cond
    (< modifier 0) (str " " modifier)
    (> modifier 0) (str " +" modifier)
    :else ""))

(s/defn rolled->message :- s/Str
  [{:keys [roll results]} :- schemas.models/Rolled]
  (let [{:keys [each modifier total]} results
        {:keys [nick username channel]} (get-in roll [:command :user])]
    (selmer/render
     (case channel
       :discord (str "*{{username}} rolled {{command}}*\n"
                     "`[{{each}}]{{modifier}}`\n"
                     "**total: {{total}}**\n")
       :slack (str "_{{username}} rolled {{command}}_\n"
                   "`[{{each}}]{{modifier}}`\n"
                   "*total: {{total}}*\n")
       :telegram (str "<i>{{username}} rolled {{command}}</i>\n"
                      "<pre>[{{each}}]{{modifier}}</pre>\n"
                      "<b>total: {{total}}</b>\n"))
     {:username (->name username nick)
      :command (get-in roll [:command :command])
      :each (string/join "," each)
      :modifier (->modifier modifier)
      :total total})))

(s/defn roll-command->error-message :- s/Str
  [{:keys [user command]} :- schemas.models/RollCommand]
  (let [{:keys [nick username channel]} user]
    (selmer/render
     (case channel
       :discord (str "{{username}} the command *{{command}}* is invalid\n"
                     "{{help|safe}}")
       :slack (str "{{username}} the command *{{command}}* is invalid\n"
                   "{{help|safe}}")
       :telegram (str "{{username}} the command <i>{{command}}</i> is invalid\n"
                      "{{help|safe}}"))
     {:username (->name username nick)
      :command command
      :help (messages/help-roll channel)})))

(s/defn ^:private roll-command-result->message :- s/Str
  [{:keys [command results]} :- schemas.models/RollCommandResults
   channel :- schemas.models/Channel]
  (let [{:keys [each modifier total]} results]
    (selmer/render
     (case channel
       :discord "`{{command}}: [{{each}}]{{modifier}} = {{total}}`\n"
       :slack "`{{command}}: [{{each}}]{{modifier}} = {{total}}`\n"
       :telegram "<pre>{{command}}: [{{each}}]{{modifier}} = {{total}}</pre>\n")
     {:command command
      :each (string/join "," each)
      :modifier (->modifier modifier)
      :total total})))

(s/defn user-command-history->message :- s/Str
  [{:keys [user history]} :- schemas.models/UserCommandHistory]
  (let [{:keys [nick username channel]} user]
    (selmer/render
     (case channel
       :discord (str "*{{username}} history*\n"
                     "{{history|safe}}")
       :slack (str "_{{username}} history_\n"
                   "{{history|safe}}")
       :telegram (str "<i>{{username}} history</i>\n"
                      "{{history|safe}}"))
     {:username (->name username nick)
      :history (if (> (count history) 0)
                 (apply str (mapv #(roll-command-result->message % channel) history))
                 "is empty\n")})))
