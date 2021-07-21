(ns integration.super-dice-roll.db-test
  (:require [clojure.test :as clojure.test]
            [com.stuartsierra.component :as component]
            [integration.super-dice-roll.util :as util]
            [parenthesin.components.config :as components.config]
            [parenthesin.components.database :as components.database]
            [schema-generators.generators :as g]
            [schema.test :as schema.test]
            [state-flow.api :refer [defflow]]
            [state-flow.assertions.matcher-combinators :refer [match?]]
            [state-flow.core :as state-flow :refer [flow]]
            [state-flow.state :as state]
            [super-dice-roll.db :as db]
            [super-dice-roll.schemas.models :as schemas.models]))

(clojure.test/use-fixtures :once schema.test/validate-schemas)

(defn- create-and-start-components! []
  (component/start-system
   (component/system-map
    :config (components.config/new-config)
    :database (component/using (components.database/new-database)
                               [:config]))))

(g/generate schemas.models/Rolled)

(defflow
  flow-integration-db-test
  {:init (util/start-system! create-and-start-components!)
   :cleanup util/stop-system!
   :fail-fast? true}
  (flow "creates a table, insert data and checks return in the database"
    [database (state-flow.api/get-state :database)
     :let [user {:id "1337"
                 :username "leetcrew"
                 :nick "leet"
                 :channel :discord}
           roll {:roll {:command {:user user
                                  :command "4d6+5"}
                        :times 4
                        :dice 6
                        :modifier +5}
                 :results {:each [4 1 6 2]
                           :modifier 5
                           :total 18}}]]

    (state/invoke #(db/insert-new-roll! roll database))

    (state/invoke
     #(dotimes [_ 12]
        (db/insert-new-roll! (assoc-in roll [:roll :command :user :id] "7331")
                             database)))

    (flow "check roll was inserted in db this user"
      (match? {:user user
               :history [{:command "4d6+5"
                          :results {:total 18 :modifier 5 :each [4 1 6 2]}}]}
              (db/get-user-channel-rolls user database)))

    (flow "check trigger is work and user has 10 rolls only"
      (match? 10
              (-> (assoc user :id "7331")
                  (db/get-user-channel-rolls database)
                  :history
                  count)))))
