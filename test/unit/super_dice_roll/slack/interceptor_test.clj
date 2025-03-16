(ns unit.super-dice-roll.slack.interceptor-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [matcher-combinators.test :refer [match?]]
            [schema.test :as schema.test]
            [super-dice-roll.slack.interceptor :as slack.interceptor]))

(use-fixtures :once schema.test/validate-schemas)

(def interceptor-fn (-> (slack.interceptor/authentication-interceptor) :enter))
(defn build-ctx [signing-secret timestamp body signature]
  {:response {:status 200}
   :request {:components {:config {:config {:slack {:signing-secret signing-secret}}}}
             :headers {"x-slack-signature" signature
                       "x-slack-request-timestamp" timestamp}}
   :raw-body body})

(deftest verify-request-test
  (let [slack-signing-secret "8f742231b10e8888abcd99yyyzzz85a5"
        timestamp 1531420618
        timestamp+one-min (+ timestamp (* 60 1))
        timestamp+six-mins (+ timestamp (* 60 6))
        body "token=xyzz0WbapA4vBCDEFasx0q6G&team_id=T1DC2JH3J&team_domain=testteamnow&channel_id=G8PSS9T3V&channel_name=foobar&user_id=U2CERLKJA&user_name=roadrunner&command=%2Fwebhook-collect&text=&response_url=https%3A%2F%2Fhooks.slack.com%2Fcommands%2FT1DC2JH3J%2F397700885554%2F96rGlfmibIGlgcZRskXaIFfN&trigger_id=398738663015.47445629121.803a0bc887a14d10d2c447fce8b6703c"
        signature "v0=a2114d57b48eac39b9ad189dd8316235a7b4a8d21a10bd27519666489c69b503"]
    (testing "interceptor should check signature vs public-key, timestamp and body"
      (with-redefs [slack.interceptor/now-secs (fn [] timestamp+one-min)]
        (is (match? {:response {:status 200}}
                    (interceptor-fn (build-ctx slack-signing-secret
                                               (str timestamp)
                                               body
                                               signature)))))

      (with-redefs [slack.interceptor/now-secs (fn [] timestamp+six-mins)]
        (is (match? {:response {:status 401}}
                    (interceptor-fn (build-ctx slack-signing-secret
                                               (str timestamp)
                                               body
                                               signature)))))

      (with-redefs [slack.interceptor/now-secs (fn [] timestamp+one-min)]
        (is (match? {:response {:status 401}}
                    (interceptor-fn (build-ctx slack-signing-secret
                                               (str timestamp)
                                               (str body "hackedbody")
                                               signature))))))))
