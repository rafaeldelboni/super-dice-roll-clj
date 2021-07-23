(ns unit.super-dice-roll.adapters-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as properties]
            [schema-generators.generators :as g]
            [schema.core :as s]
            [schema.test :as schema.test]
            [super-dice-roll.adapters :as adapters]
            [super-dice-roll.schemas.db :as schemas.db]
            [super-dice-roll.schemas.models :as schemas.models]
            [super-dice-roll.schemas.types :as schemas.types]))

(use-fixtures :once schema.test/validate-schemas)

(defspec rolled-db-new-roll-test 50
  (properties/for-all [rolled (g/generator schemas.models/Rolled schemas.types/TypesLeafGenerators)]
    (s/validate schemas.db/NewRoll (adapters/rolled->db-new-roll rolled))))

(defspec db-user-command-history-test 50
  (properties/for-all [rolls (g/generator [schemas.db/Roll] schemas.types/TypesLeafGenerators)
                       user (g/generator schemas.models/User schemas.types/TypesLeafGenerators)]
    (s/validate schemas.models/UserCommandHistory (adapters/db->user-command-history rolls user))))

(deftest ->name-test
  (testing "should get the current user"
    (is (= "bbb" (adapters/->name "aaa" "bbb")))
    (is (= "bbb" (adapters/->name "" "bbb")))
    (is (= "aaa" (adapters/->name "aaa" "")))
    (is (= "" (adapters/->name "" "")))))

(deftest ->modifier-test
  (testing "should get the modifier as str and sign"
    (is (= " +2" (adapters/->modifier 2)))
    (is (= " -2" (adapters/->modifier -2)))
    (is (= "" (adapters/->modifier 0)))))
