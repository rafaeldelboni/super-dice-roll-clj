(ns unit.super-dice-roll.slack.adapters-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [matcher-combinators.test :refer [match?]]
            [schema.test :as schema.test]
            [super-dice-roll.slack.adapters :as adapters]))

(use-fixtures :once schema.test/validate-schemas)

(def update-msg-1
  {:token "gIkuvaNzQIHg97ATvDxqgjtO"
   :team_id "T0001"
   :team_domain "example"
   :enterprise_id "E0001"
   :enterprise_name "Globular%20Construct%20Inc"
   :channel_id "C2147483705"
   :channel_name "test"
   :user_id "U2147483697"
   :user_name "Steve"
   :command "/roll"
   :text "6d4+5"
   :response_url "https://hooks.slack.com/commands/1234/5678"
   :trigger_id "13345224609.738474920.8088930838d88f008e0"
   :api_app_id "A123456"})

(deftest wire-in->user-test
  (testing "should get the current user"
    (is (match? {:id "U2147483697"
                 :username "Steve"
                 :nick ""}
                (adapters/wire-in->user update-msg-1)))))

(deftest wire-in->model-test
  (testing "should adapt update into roll"
    (is (match? {:user {:id "U2147483697"
                        :username "Steve"
                        :nick ""}
                 :command "6d4+5"}
                (adapters/wire-in->model update-msg-1))))

  (testing "should adapt empty rolls"
    (is (match? {:user {:id "U2147483697"
                        :username "Steve"
                        :nick ""}
                 :command ""}
                (adapters/wire-in->model (dissoc update-msg-1 :text))))))
