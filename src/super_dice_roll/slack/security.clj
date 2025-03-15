(ns super-dice-roll.slack.security
  (:require [clojure.string :as str]
            [super-dice-roll.adapters.bytes :as adapters.bytes])
  (:import [org.bouncycastle.crypto.digests SHA256Digest]
           [org.bouncycastle.crypto.macs HMac]
           [org.bouncycastle.crypto.params KeyParameter]))

(def version "v0")

(def separator ":")

(defn hash-hmac-sha256
  "generate HMac SHA256 signature for data and key byte arrays.
  return byte array with signature."
  [^String data ^String key]
  (let [digest (new SHA256Digest)
        hmac (new HMac digest)
        data-bytes (.getBytes data "utf8")
        key-bytes (.getBytes key "utf8")
        hmac-out (byte-array (.getMacSize hmac))]

    (.init hmac (new KeyParameter key-bytes))
    (.update hmac data-bytes 0 (alength data-bytes))
    (.doFinal hmac hmac-out 0)
    hmac-out))

(defn verify-request
  "verify slack payload with app signing-secret, request body,
  signature and timestamp headers.
  return boolean with verification.
  https://api.slack.com/authentication/verifying-requests-from-slack"
  [^String signing-secret ^Number now ^Number timestamp ^String body ^String signature]
  (let [basestring (str/join separator [version timestamp body])
        hash (adapters.bytes/bytes->hex (hash-hmac-sha256 basestring signing-secret))]
    (and
     (> (* 60 5) (- now timestamp))
     (= (str version "=" hash) signature))))
