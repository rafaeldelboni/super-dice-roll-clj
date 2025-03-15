(ns super-dice-roll.discord.security
  (:require [super-dice-roll.adapters.bytes :as adapters.bytes])
  (:import (java.security SecureRandom)
           (org.bouncycastle.crypto.generators Ed25519KeyPairGenerator)
           (org.bouncycastle.crypto.params Ed25519KeyGenerationParameters Ed25519PrivateKeyParameters Ed25519PublicKeyParameters)
           (org.bouncycastle.crypto.signers Ed25519Signer)))

(defn generate-keypair
  "generate Ed25519 key pair.
  return {:private `Ed25519PrivateKeyParameters`
          :public `Ed25519PublicKeyParameters`}"
  []
  (let [random (SecureRandom.)
        kpg    (Ed25519KeyPairGenerator.)
        _ (.init kpg (Ed25519KeyGenerationParameters. random))
        key-pair (.generateKeyPair kpg)]
    {:private (cast Ed25519PrivateKeyParameters (.getPrivate key-pair))
     :public  (cast Ed25519PublicKeyParameters (.getPublic key-pair))}))

(defn new-signer
  "return new instance of `Ed25519Signer` initialized by private key"
  [private-key]
  (let [signer (Ed25519Signer.)]
    (.init signer true private-key)
    signer))

(defn sign
  "generate signature for msg byte array.
  return byte array with signature."
  [^Ed25519Signer signer msg-bytes]
  (.update signer msg-bytes 0 (alength msg-bytes))
  (.generateSignature signer))

(defn new-verifier
  "return new instance of `Ed25519Signer` initialized by public key."
  [public-key]
  (let [signer (Ed25519Signer.)]
    (.init signer false public-key)
    signer))

(defn verify
  "verify signature for msg byte array.
  return true if valid signature and false if not."
  [^Ed25519Signer signer msg-bytes signature]
  (.update signer msg-bytes 0 (alength msg-bytes))
  (.verifySignature signer signature))

(defn verify-request
  "verify discord payload with app public-key,
  request body, signature and timestamp headers"
  [public-key timestamp body signature]
  (verify (new-verifier (Ed25519PublicKeyParameters. (adapters.bytes/hex->bytes public-key) 0))
          (.getBytes (str timestamp body) "utf8")
          (adapters.bytes/hex->bytes signature)))
