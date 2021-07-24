(ns unit.super-dice-roll.telegram.adapters-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [matcher-combinators.test :refer [match?]]
            [schema.test :as schema.test]
            [super-dice-roll.telegram.adapters :as adapters]))

(use-fixtures :once schema.test/validate-schemas)

(def update-msg-1
  {:update_id 894745141
   :message {:date 1626904234,
             :entities [{:offset 0
                         :type "bot_command"
                         :length 5}]
             :chat {:first_name "Dongo"
                    :username "dongomagolo"
                    :type "private"
                    :id 1234567
                    :last_name "Magolo"}
             :message_id 17
             :from {:first_name "Dongo"
                    :language_code "en"
                    :is_bot false
                    :username "dongomagolo"
                    :id 7654321
                    :last_name "Magolo"}
             :text "/roll 6d4+5"}})

(deftest wire-in->user-test
  (testing "should get the current user"
    (is (match? {:id "7654321"
                 :username "dongomagolo"
                 :nick ""}
                (adapters/wire-in->user update-msg-1)))))

(deftest wire-in->model-test
  (testing "should adapt update into roll"
    (is (match? {:user {:id "7654321"
                        :username "dongomagolo"
                        :nick ""}
                 :command "6d4+5"}
                (adapters/wire-in->model update-msg-1)))))
