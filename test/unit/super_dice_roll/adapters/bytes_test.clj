(ns unit.super-dice-roll.adapters.bytes-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [schema.test :as schema.test]
            [super-dice-roll.adapters.bytes :as adapter.bytes]))

(use-fixtures :once schema.test/validate-schemas)

(deftest hex->bytes-test
  (testing "should convert to byte array"
    (is (= (seq [115 108 97 99 107 98 111 116])
           (seq (adapter.bytes/hex->bytes "736c61636b626f74"))))))

(deftest bytes->hex-test
  (testing "should convert to byte array"
    (is (= "736c61636b626f74"
           (adapter.bytes/bytes->hex [115 108 97 99 107 98 111 116])))))
