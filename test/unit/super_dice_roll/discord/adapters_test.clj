(ns unit.super-dice-roll.discord.adapters-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [matcher-combinators.test :refer [match?]]
            [schema.test :as schema.test]
            [super-dice-roll.discord.adapters :as adapters]))

(use-fixtures :once schema.test/validate-schemas)

(def guild-request-1 {:guild_id "853711282723880895"
                      :type 2
                      :channel_id "85371128272388091"
                      :member {:deaf false
                               :nick nil
                               :permissions "137438953564"
                               :pending false
                               :premium_since nil
                               :roles ["853711852508151906"]
                               :is_pending false
                               :avatar nil
                               :joined_at "2021-06-13T19:04:35.870000+00:00"
                               :user {:username "dombelombers"
                                      :id "598978693322375444"
                                      :avatar "42fe6008314f0f977ee9e9166cf261ff"
                                      :public_flags 0
                                      :discriminator "4261"}
                               :mute false}
                      :token "aW50ZXJhY3Rpb246ODYzNDEzMDA1NTc0OTk1OTc5OlBGaTg1"
                      :id "863413005574996100"
                      :application_id "861964097700757703"
                      :version 1
                      :data {:name "roll"
                             :id "86212849198956528"
                             :options [{:name "dice"
                                        :value "3d6+1"
                                        :type 3}]}})

(deftest wire-in->user-test
  (testing "should get the current user"
    (is (match? {:id "598978693322375444"
                 :username "dombelombers"
                 :nick ""}
                (adapters/wire-in->user guild-request-1)))))

(deftest wire-in->model-test
  (testing "should adapt interaction into roll"
    (is (match? {:user {:id "598978693322375444"
                        :username "dombelombers"
                        :nick ""}
                 :command "3d6+1"}
                (adapters/wire-in->model guild-request-1)))))
