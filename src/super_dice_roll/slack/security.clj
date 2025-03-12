(ns super-dice-roll.slack.security
  (:require [clojure.string :as str])
  (:import [org.bouncycastle.crypto.digests SHA256Digest]
           [org.bouncycastle.crypto.macs HMac]
           [org.bouncycastle.crypto.params KeyParameter]))

;; TODO
;; https://api.slack.com/authentication/verifying-requests-from-slack
;; https://api.slack.com/interactivity/slash-commands

;; TODO move this to base adapters/byte ns
(defn bytes->hex
  "convert byte array to hex string."
  [^bytes byte-array]
  (let [hex [\0 \1 \2 \3 \4 \5 \6 \7 \8 \9 \a \b \c \d \e \f]]
    (letfn [(hexify-byte [b]
              (let [v (bit-and b 0xFF)]
                [(hex (bit-shift-right v 4)) (hex (bit-and v 0x0F))]))]
      (str/join (mapcat hexify-byte byte-array)))))

(defn sign-hmac-sha256
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

(comment
  ;; tests
  (def slack-signing-secret "8f742231b10e8888abcd99yyyzzz85a5")
  (def sig-basestring "v0:1531420618:token=xyzz0WbapA4vBCDEFasx0q6G&team_id=T1DC2JH3J&team_domain=testteamnow&channel_id=G8PSS9T3V&channel_name=foobar&user_id=U2CERLKJA&user_name=roadrunner&command=%2Fwebhook-collect&text=&response_url=https%3A%2F%2Fhooks.slack.com%2Fcommands%2FT1DC2JH3J%2F397700885554%2F96rGlfmibIGlgcZRskXaIFfN&trigger_id=398738663015.47445629121.803a0bc887a14d10d2c447fce8b6703c")
  (= (bytes->hex (sign-hmac-sha256 sig-basestring slack-signing-secret))
     "a2114d57b48eac39b9ad189dd8316235a7b4a8d21a10bd27519666489c69b503"))
