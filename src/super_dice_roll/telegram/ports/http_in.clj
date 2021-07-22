(ns super-dice-roll.telegram.ports.http-in
  (:require [parenthesin.logs :as logs]
            [super-dice-roll.telegram.ports.http-out :as telegram.ports.http-out]))

(defn process-update!
  [{{path :path
     body :body
     headers :header} :parameters
    components :components}]
  (logs/log :info {:channel :telegram :header headers :path path :body body})
  (let [message-id (get-in body [:message :from :id])
        slash-cmd (get-in body [:message :text])]
    (condp #(seq (re-find %1 %2)) slash-cmd
      #"^/roll" (telegram.ports.http-out/send-message "roll" message-id components)
      #"^/history" (telegram.ports.http-out/send-message "history" message-id components)
      #"^/help" (telegram.ports.http-out/send-message "help" message-id components)
      (telegram.ports.http-out/send-message "help2" message-id components)))
  {:status 200
   :body {}})
