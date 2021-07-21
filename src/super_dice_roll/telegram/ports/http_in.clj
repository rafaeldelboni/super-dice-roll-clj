(ns super-dice-roll.telegram.ports.http-in
  (:require [parenthesin.logs :as logs]))

(defn process-update!
  [{{body :body headers :header} :parameters
    _components :components}]
  (logs/log :info {:header headers :body body})
  {:status 200
   :body {}})
