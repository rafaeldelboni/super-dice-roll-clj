(ns super-dice-roll.controllers
  (:require [schema.core :as s]
            [super-dice-roll.db :as base.db]
            [super-dice-roll.logics :as base.logics]
            [super-dice-roll.schemas.models :as schemas.models]
            [super-dice-roll.schemas.types :as schemas.types]))

(defn- rand-int-range [a b]
  (int (+ a (rand (- b a)))))

(s/defn roll->rolled :- schemas.models/Rolled
  [{:keys [times dice modifier] :as roll} :- schemas.models/Roll]
  (let [results (mapv (fn [_] (rand-int-range 1 (inc dice)))
                      (range 0 times))]
    {:roll roll
     :results {:each  results
               :total (-> (reduce + results) (+ modifier))}}))

(s/defn do-roll! :- (s/maybe schemas.models/Rolled)
  [roll-command :- schemas.models/RollCommand
   {:keys [database]} :- schemas.types/Components]
  (let [roll (base.logics/roll-command->roll roll-command)]
    (when (base.logics/valid-roll? roll)
      (let [rolled (roll->rolled roll)]
        (future (base.db/insert-new-roll! rolled database))
        rolled))))
