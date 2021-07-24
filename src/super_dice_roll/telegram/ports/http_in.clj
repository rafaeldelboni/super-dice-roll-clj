(ns super-dice-roll.telegram.ports.http-in
  (:require [super-dice-roll.adapters :as base.adapters]
            [super-dice-roll.controllers :as base.controller]
            [super-dice-roll.messages :as messages]
            [super-dice-roll.telegram.adapters :as telegram.adapters]
            [super-dice-roll.telegram.ports.http-out :as telegram.ports.http-out]))

(defn process-update!
  [{{body :body} :parameters
    components :components}]
  (let [message-id (get-in body [:message :chat :id])
        slash-cmd (get-in body [:message :text])
        message (condp #(seq (re-find %1 %2)) slash-cmd
                  #"^/roll" (let [roll-cmd (telegram.adapters/wire-in->model body)
                                  rolled (base.controller/do-roll! roll-cmd components)]
                              (if rolled
                                (base.adapters/rolled->message rolled)
                                (base.adapters/roll-command->error-message roll-cmd)))
                  #"^/history" (-> (telegram.adapters/wire-in->user body)
                                   (base.controller/get-user-command-history components)
                                   base.adapters/user-command-history->message)
                  #"^/help" (messages/help :telegram)
                  (str slash-cmd " is a invalid command\n"
                       (messages/help :telegram)))]
    (telegram.ports.http-out/send-message message message-id components))
  {:status 200
   :body {}})
