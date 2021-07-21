(ns super-dice-roll.telegram.ports.http-in
  (:require [parenthesin.logs :as logs]))

(defn process-update!
  [{{path :path
     body :body
     headers :header} :parameters
    _components :components}]
  (logs/log :info {:channel :telegram :header headers :path path :body body})
  {:status 200
   :body {}})
