(ns super-dice-roll.telegram.ports.http-in
  (:require [super-dice-roll.adapters :as base.adapters]
            [super-dice-roll.controllers :as base.controller]
            [super-dice-roll.messages :as messages]
            [super-dice-roll.telegram.adapters :as telegram.adapters]
            [super-dice-roll.telegram.ports.http-out :as telegram.ports.http-out]))

(defn ^:private prefix-match [sorted-map target]
  (let [[closest-match value] (first (rsubseq sorted-map <= target))]
    (if closest-match
      (if (.startsWith target closest-match)
        value
        nil)
      nil)))

(defn process-update!
  [{{body :body} :parameters
    components :components}]
  (let [message-id (or (get-in body [:message :chat :id]) 0)
        slash-cmd (or (get-in body [:message :text]) "")
        command (prefix-match (sorted-map "/roll" :roll
                                          "/history" :history
                                          "/help" :help)
                              slash-cmd)
        message (case command
                  :roll (let [roll-cmd (telegram.adapters/wire-in->model body)
                              rolled (base.controller/do-roll! roll-cmd components)]
                          (if rolled
                            (base.adapters/rolled->message rolled)
                            (base.adapters/roll-command->error-message roll-cmd)))
                  :history (-> (telegram.adapters/wire-in->user body)
                               (base.controller/get-user-command-history components)
                               base.adapters/user-command-history->message)
                  :help (messages/help :telegram)
                  nil)]
    (when message
      (telegram.ports.http-out/send-message message message-id components)))
  {:status 200
   :body {}})
