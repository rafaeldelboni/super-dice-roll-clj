(ns super-dice-roll.schemas.types
  (:require [cheshire.core :as json]
            [clojure.string :as str]
            [clojure.test.check.generators :as generators]
            [com.stuartsierra.component :as component]
            [parenthesin.components.database :as components.database]
            [parenthesin.components.http :as components.http]
            [schema.core :as s]))

(def HttpComponent (s/protocol components.http/HttpProvider))

(def DatabaseComponent (s/protocol components.database/DatabaseProvider))

(s/defschema Components
  {:config (s/protocol component/Lifecycle)
   :http HttpComponent
   :router (s/protocol component/Lifecycle)
   :database DatabaseComponent})

(def json-array-int?
  (fn [field]
    (->> field
         json/decode
         (s/validate [s/Int]))))

(def JsonArrayInt (s/pred json-array-int? "JSON Int Array. Eg: [1, 4, 5]"))

(def JsonArrayIntGenerator
  (generators/fmap #(str "[" (str/join "," %) "]")
                   (generators/vector (generators/choose 1 20) (rand-int 6))))

(def TypesLeafGenerators
  {JsonArrayInt JsonArrayIntGenerator})
