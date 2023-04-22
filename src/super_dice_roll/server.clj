(ns super-dice-roll.server
  (:require [com.stuartsierra.component :as component]
            [parenthesin.components.config.aero :as config]
            [parenthesin.components.db.jdbc-hikari :as database]
            [parenthesin.components.http.clj-http :as http]
            [parenthesin.components.server.reitit-pedestal-jetty :as webserver]
            [parenthesin.helpers.logs :as logs]
            [parenthesin.helpers.migrations :as migrations]
            [super-dice-roll.components.router :as router]
            [super-dice-roll.routes :as routes])
  (:gen-class))

(def system-atom (atom nil))

(defn- build-system-map []
  (component/system-map
   :config (config/new-config)
   :http (http/new-http)
   :router (router/new-router routes/routes)
   :database (component/using (database/new-database) [:config])
   :webserver (component/using (webserver/new-webserver) [:config :http :router :database])))

(defn start-system! [system-map]
  (logs/setup :info :auto)
  (migrations/migrate (migrations/configuration-with-db))
  (->> system-map
       component/start
       (reset! system-atom)))

(defn stop-system! []
  (logs/log :info :system-stop)
  (swap!
   system-atom
   (fn [s] (when s (component/stop s)))))

(defn -main
  "The entry-point for 'gen-class'"
  [& _args]
  ; Graceful shutdown
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. ^Runnable stop-system!))
  (start-system! (build-system-map)))

(comment
  (stop-system!)
  (start-system! (build-system-map)))
