(ns super-dice-roll.adapters.bytes
  (:require [clojure.string :as str]))

(defn bytes->hex
  "convert byte array to hex string."
  [^bytes byte-array]
  (let [hex [\0 \1 \2 \3 \4 \5 \6 \7 \8 \9 \a \b \c \d \e \f]]
    (letfn [(hexify-byte [b]
              (let [v (bit-and b 0xFF)]
                [(hex (bit-shift-right v 4)) (hex (bit-and v 0x0F))]))]
      (str/join (mapcat hexify-byte byte-array)))))

(defn hex->bytes
  "convert hex string to byte array."
  [^String hex-string]
  (letfn [(unhexify-2 [^Character c1 ^Character c2]
            (unchecked-byte
             (+ (bit-shift-left (Character/digit c1 16) 4)
                (Character/digit c2 16))))]
    (byte-array (map #(apply unhexify-2 %) (partition 2 hex-string)))))
