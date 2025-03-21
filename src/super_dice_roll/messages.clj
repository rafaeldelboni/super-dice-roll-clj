(ns super-dice-roll.messages
  (:require [schema.core :as s]
            [super-dice-roll.schemas.models :as schemas.models]))

(s/defn help-header :- s/Str
  [channel :- schemas.models/Channel]
  (case channel
    :discord "Available commands: `/roll`, `/history` or `/help`\n"
    :slack "Available commands: `/roll`, `/history` or `/help`\n"
    :telegram "Available commands: <pre>/roll</pre>, <pre>/history</pre> or <pre>/help</pre>\n"))

(s/defn help-roll :- s/Str
  [channel :- schemas.models/Channel]
  (case channel
    :discord (str "`/roll <NDM>`\n"
                  "You must specify dice and modifiers in following format:\n"
                  "N = Number of dices\n"
                  "D = Dice type (D6, D12, D20)\n"
                  "M = Modifiers (+1, -3)\n"
                  "Example: `/roll 3D6+3`\n")
    :slack (str "`/roll <NDM>`\n"
                "You must specify dice and modifiers in following format:\n"
                "N = Number of dices\n"
                "D = Dice type (D6, D12, D20)\n"
                "M = Modifiers (+1, -3)\n"
                "Example: `/roll 3D6+3`\n")
    :telegram (str "<pre>/roll &lt;NDM&gt;</pre>\n"
                   "You must specify dice and modifiers in following format:\n"
                   "N = Number of dices\n"
                   "D = Dice type (D6, D12, D20)\n"
                   "M = Modifiers (+1, -3)\n"
                   "Example: <pre>/roll 3D6+3</pre>\n")))

(s/defn help-history :- s/Str
  [channel :- schemas.models/Channel]
  (case channel
    :discord (str "`/history`\n"
                  "Lists your lasts 10 rolls with the results.\n")
    :slack (str "`/history`\n"
                "Lists your lasts 10 rolls with the results.\n")
    :telegram (str "<pre>/history</pre>\n"
                   "Lists your lasts 10 rolls with the results.\n")))

(s/defn help-footer :- s/Str
  [channel :- schemas.models/Channel]
  (case channel
    :discord "[Source Code](https://j.mp/sdr-bot)\n"
    :slack "<https://j.mp/sdr-bot|Source Code>\n"
    :telegram "<a href=\"https://j.mp/sdr-bot\">Source Code</a>\n"))

(s/defn help :- s/Str
  [channel :- schemas.models/Channel]
  (str (help-header channel) "\n"
       (help-roll channel) "\n"
       (help-history channel) "\n"
       (help-footer channel)))
