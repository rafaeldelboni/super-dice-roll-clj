(ns unit.super-dice-roll.telegram.interceptor-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [matcher-combinators.test :refer [match?]]
            [schema.test :as schema.test]
            [super-dice-roll.telegram.interceptor :as telegram.interceptor]))

(use-fixtures :once schema.test/validate-schemas)

(def interceptor-fn (-> (telegram.interceptor/verification-interceptor) :enter))
(defn build-ctx [bot-token bot-token-request]
  {:response {:status 200}
   :request {:components {:config {:config {:telegram {:bot-token bot-token-request}}}}
             :parameters {:path {:bot-token bot-token}}}})

(deftest verify-request-test
  (let [bot-token "bot-pipipi-popopo-token"]
    (testing "interceptor should check signature vs public-key, timestamp and body"
      (is (match? {:response {:status 200}}
                  (interceptor-fn (build-ctx bot-token "bot-pipipi-popopo-token"))))
      (is (match? {:response {:status 401}}
                  (interceptor-fn (build-ctx bot-token "invalid-pipipi-popopo-token")))))))
