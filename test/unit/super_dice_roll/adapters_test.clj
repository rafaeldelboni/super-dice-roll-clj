(ns unit.super-dice-roll.adapters-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as properties]
            [schema-generators.generators :as g]
            [schema.core :as s]
            [schema.test :as schema.test]
            [super-dice-roll.adapters :as adapters]
            [super-dice-roll.messages :as messages]
            [super-dice-roll.schemas.db :as schemas.db]
            [super-dice-roll.schemas.models :as schemas.models]
            [super-dice-roll.schemas.types :as schemas.types]))

(use-fixtures :once schema.test/validate-schemas)

(defspec rolled-db-new-roll-test 50
  (properties/for-all [rolled (g/generator schemas.models/Rolled
                                           schemas.types/TypesLeafGenerators)]
    (s/validate schemas.db/NewRoll
                (adapters/rolled->db-new-roll rolled))))

(defspec db-user-command-history-test 50
  (properties/for-all [rolls (g/generator [schemas.db/Roll]
                                          schemas.types/TypesLeafGenerators)
                       user (g/generator schemas.models/User
                                         schemas.types/TypesLeafGenerators)]
    (s/validate schemas.models/UserCommandHistory
                (adapters/db->user-command-history rolls user))))

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

(deftest rolled->message-test
  (testing "adapt rolled results into output message"
    (let [rolled {:roll {:command {:user {:id "12345678"
                                          :username "usernola"
                                          :nick ""
                                          :channel :discord}
                                   :command "2d12+5"}
                         :times 2
                         :dice 12
                         :modifier 5}
                  :results {:each [4, 7]
                            :modifier 5
                            :total 16}}]

      (is (= "*nicola rolled 2d12+5*\n`[4,7] +5`\n**total: 16**\n"
             (adapters/rolled->message
              (assoc-in rolled [:roll :command :user :nick] "nicola")))
          "should show nick and modifier discord")
      (is (= "*usernola rolled 2d12+5*\n`[4,7] +5`\n**total: 16**\n"
             (adapters/rolled->message
              rolled))
          "should show username and modifier discord")
      (is (= "*usernola rolled 2d12-7*\n`[4,7] -7`\n**total: 4**\n"
             (adapters/rolled->message
              (-> rolled
                  (assoc-in [:roll :command :command] "2d12-7")
                  (assoc-in [:results :modifier] -7)
                  (assoc-in [:results :total] 4))))
          "should show username and negative modifier discord")
      (is (= "*usernola rolled 2d12+5*\n`[4,7]`\n**total: 11**\n"
             (adapters/rolled->message
              (-> rolled
                  (assoc-in [:roll :command :command] "2d12+5")
                  (assoc-in [:results :modifier] 0)
                  (assoc-in [:results :total] 11))))
          "should show username and no modifier discord")

      (is (= "<i>nicola rolled 2d12+5</i>\n<pre>[4,7] +5</pre>\n<b>total: 16</b>\n"
             (adapters/rolled->message
              (-> rolled
                  (assoc-in [:roll :command :user :channel] :telegram)
                  (assoc-in [:roll :command :user :nick] "nicola"))))
          "should show nick and modifier telegram")
      (is (= "<i>usernola rolled 2d12+5</i>\n<pre>[4,7] +5</pre>\n<b>total: 16</b>\n"
             (adapters/rolled->message
              (assoc-in rolled [:roll :command :user :channel] :telegram)))
          "should show username and modifier telegram")
      (is (= "<i>usernola rolled 2d12-7</i>\n<pre>[4,7] -7</pre>\n<b>total: 4</b>\n"
             (adapters/rolled->message
              (-> rolled
                  (assoc-in [:roll :command :user :channel] :telegram)
                  (assoc-in [:roll :command :command] "2d12-7")
                  (assoc-in [:results :modifier] -7)
                  (assoc-in [:results :total] 4))))
          "should show username and negative modifier telegram")
      (is (= "<i>usernola rolled 2d12+5</i>\n<pre>[4,7]</pre>\n<b>total: 11</b>\n"
             (adapters/rolled->message
              (-> rolled
                  (assoc-in [:roll :command :user :channel] :telegram)
                  (assoc-in [:roll :command :command] "2d12+5")
                  (assoc-in [:results :modifier] 0)
                  (assoc-in [:results :total] 11))))
          "should show username and no modifier telegram"))))

(defspec rolled-message-generative-test 50
  (properties/for-all [rolled (g/generator schemas.models/Rolled)]
    (s/validate s/Str (adapters/rolled->message rolled))))

(deftest roll-command-error-message-test
  (testing "adapt roll command into error message"
    (is (= (str "wararana the command *wreberwreber* is invalid\n"
                (messages/help-roll :discord))
           (adapters/roll-command->error-message {:user {:id "123456789"
                                                         :username "dombelombers"
                                                         :nick "wararana"
                                                         :channel :discord}
                                                  :command "wreberwreber"}))
        "should show show nick and error for discord")

    (is (= (str "dombelombers the command *wreberwreber* is invalid\n"
                (messages/help-roll :discord))
           (adapters/roll-command->error-message {:user {:id "123456789"
                                                         :username "dombelombers"
                                                         :nick ""
                                                         :channel :discord}
                                                  :command "wreberwreber"}))
        "should show show username and error for discord")

    (is (= (str "dombelombers the command <i>wreberwreber</i> is invalid\n"
                (messages/help-roll :telegram))
           (adapters/roll-command->error-message {:user {:id "123456789"
                                                         :username "dombelombers"
                                                         :nick ""
                                                         :channel :telegram}
                                                  :command "wreberwreber"}))
        "should show show username and error for telegram")))

(defspec roll-command-error-message-generative-test 50
  (properties/for-all [roll-cmd (g/generator schemas.models/RollCommand)]
    (s/validate s/Str (adapters/roll-command->error-message roll-cmd))))

(deftest user-command-history-message-test
  (testing "adapt user command history into message"
    (let [user-history {:user
                        {:id "123456789"
                         :username "dombelombers"
                         :nick "wararana"
                         :channel :discord},
                        :history
                        [{:command "d12"
                          :results {:each [7]
                                    :modifier 0
                                    :total 7}}
                         {:command "2d6"
                          :results {:each [2 4]
                                    :modifier 0
                                    :total 6}}
                         {:command "4d6+4"
                          :results {:each [2 4 1 3]
                                    :modifier 4
                                    :total 14}}
                         {:command "4d6-4"
                          :results {:each [2 4 1 3]
                                    :modifier -4
                                    :total 6}}]}]

      (is (= (str "*wararana history*\n`d12: [7] = 7`\n`2d6: [2,4] = 6`\n"
                  "`4d6+4: [2,4,1,3] +4 = 14`\n`4d6-4: [2,4,1,3] -4 = 6`\n")
             (adapters/user-command-history->message user-history))
          "should show user history discord")

      (is (= (str "<i>wararana history</i>\n<pre>d12: [7] = 7</pre>\n<pre>2d6: [2,4] = 6</pre>\n"
                  "<pre>4d6+4: [2,4,1,3] +4 = 14</pre>\n<pre>4d6-4: [2,4,1,3] -4 = 6</pre>\n")
             (adapters/user-command-history->message
              (assoc-in user-history [:user :channel] :telegram)))
          "should show user history telegram"))))

(defspec user-command-history-message-generative-test 50
  (properties/for-all [user-cmd-history (g/generator schemas.models/UserCommandHistory)]
    (s/validate s/Str (adapters/user-command-history->message user-cmd-history))))
