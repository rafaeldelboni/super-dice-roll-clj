(ns super-dice-roll.slack.schemas.http-in
  (:require [schema.core :as s]))

(s/defschema Command
  {(s/optional-key :token) s/Str
   (s/optional-key :team_id) s/Str
   (s/optional-key :team_domain) s/Str
   (s/optional-key :enterprise_id) s/Str
   (s/optional-key :enterprise_name) s/Str
   (s/optional-key :channel_id) s/Str
   (s/optional-key :channel_name) s/Str
   (s/optional-key :user_id) s/Str
   (s/optional-key :user_name) s/Str
   (s/optional-key :command) s/Str
   (s/optional-key :text) s/Str
   (s/optional-key :response_url) s/Str
   (s/optional-key :trigger_id) s/Str
   (s/optional-key :api_app_id) s/Str
   s/Any s/Any})
