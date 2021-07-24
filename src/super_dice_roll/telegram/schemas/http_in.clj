(ns super-dice-roll.telegram.schemas.http-in
  (:require [schema.core :as s]))

(s/defschema Chat
  {:id s/Int
   :type s/Str
   (s/optional-key :first_name) s/Str
   (s/optional-key :last_name) s/Str
   (s/optional-key :username) s/Str
   s/Any s/Any})

(s/defschema User
  {:id s/Int
   (s/optional-key :username) s/Str
   (s/optional-key :is_bot) s/Bool
   (s/optional-key :first_name) s/Str
   (s/optional-key :last_name) s/Str
   (s/optional-key :language_code) s/Str
   s/Any s/Any})

(s/defschema MessageEntity
  {:type s/Str
   (s/optional-key :offset) s/Int
   (s/optional-key :length) s/Int
   s/Any s/Any})

(s/defschema Message
  {:message_id s/Int
   :date s/Int
   :chat Chat
   (s/optional-key :from) User
   (s/optional-key :message) s/Str
   (s/optional-key :entities) [MessageEntity]
   s/Any s/Any})

(s/defschema Update
  {:update_id s/Int
   (s/optional-key :message) Message
   s/Any s/Any})
