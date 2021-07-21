(ns unit.super-dice-roll.controllers-test
  (:require [clojure.test :refer [are deftest testing]]
            [matcher-combinators.matchers :as m]
            [matcher-combinators.test :refer [match?]]
            [super-dice-roll.controllers :as controllers]
            [unit.super-dice-roll.utils :refer [->roll in-range?]]))

(deftest roll->rolled-test
  (testing "should roll dices parsed commands and sum results in total"
    (are [roll expected] (match? expected (controllers/roll->rolled (->roll roll)))
      {:times 01 :dice 06 :modifier  0} {:results {:total #(>= %  1) :each (m/embeds [(in-range?  6)])}}
      {:times 02 :dice 12 :modifier  0} {:results {:total #(>= %  2) :each (m/embeds [(in-range? 12)])}}
      {:times 03 :dice 20 :modifier  5} {:results {:total #(>= %  8) :each (m/embeds [(in-range? 20)])}}
      {:times 15 :dice 04 :modifier -5} {:results {:total #(>= % 10) :each (m/embeds [(in-range?  4)])}}
      {:times 01 :dice 00 :modifier  0} {:results {:total #(>= %  1) :each [1]}}
      {:times 01 :dice 00 :modifier  0} {:results {:total #(>= %  1) :each [1]}})))
