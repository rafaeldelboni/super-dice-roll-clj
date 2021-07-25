(ns parenthesin.components.router
  (:require [com.stuartsierra.component :as component]
            [muuntaja.core :as m]
            [parenthesin.logs :as logs]
            [reitit.coercion.schema :as reitit.schema]
            [reitit.dev.pretty :as pretty]
            [reitit.http :as http]
            [reitit.http.coercion :as coercion]
            [reitit.http.interceptors.exception :as exception]
            [reitit.http.interceptors.multipart :as multipart]
            [reitit.http.interceptors.muuntaja :as muuntaja]
            [reitit.http.interceptors.parameters :as parameters]
            [reitit.pedestal :as pedestal]
            [reitit.ring :as ring]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui])
  (:import (java.io ByteArrayOutputStream InputStream)
           (java.nio ByteBuffer)))

(defn- coercion-error-handler [status]
  (fn [exception _request]
    (logs/log :error exception :coercion-errors (:errors (ex-data exception)))
    {:status status
     :body (if (= 400 status)
             (str "Invalid path or request parameters, with the following errors: "
                  (:errors (ex-data exception)))
             "Error checking path or request parameters.")}))

(defn- exception-info-handler [exception _request]
  (logs/log :error exception "Server exception:" :exception exception)
  {:status 500
   :body   "Internal error."})

(defn- read-all-bytes
  "Reads all bytes from either an `InputStream` or a `ByteBuffer`.
  If an `InputStream` is provided, it will be consumed, but not closed.
  Returns its result as a *new* byte array."
  ^bytes [input]
  (condp instance? input
    InputStream (let [bos (ByteArrayOutputStream.)]
                  (loop [next (.read ^InputStream input)]
                    (if (== next -1)
                      (.toByteArray bos)
                      (do
                        (.write bos next)
                        (recur (.read ^InputStream input))))))
    ByteBuffer (let [len (.remaining ^ByteBuffer input)
                     result (byte-array len)]
                 (.get ^ByteBuffer input result)
                 result)))

(defn store-raw-body []
  {:name ::store-raw-body
   :enter (fn [ctx]
            (let [body (read-all-bytes (-> ctx :request :body))]
              (-> ctx
                  (assoc-in [:request :body] body)
                  (assoc :raw-body body))))})

(defn read-raw-body []
  {:name ::store-raw-body
   :enter (fn [ctx]
            (let [raw-body (slurp (:raw-body ctx))]
              (-> ctx
                  (assoc :raw-body raw-body))))})

(def router-settings
  {;:reitit.interceptor/transform dev/print-context-diffs ;; pretty context diffs
     ;;:validate spec/validate ;; enable spec validation for route data
     ;;:reitit.spec/wrap spell/closed ;; strict top-level validation
   :exception pretty/exception
   :data {:coercion reitit.schema/coercion
          :muuntaja (m/create
                     (-> m/default-options
                         (assoc-in [:formats "application/json" :decoder-opts :bigdecimals] true)))
          :interceptors [;; store body before parsing/coersion
                         (store-raw-body)
                             ;; swagger feature
                         swagger/swagger-feature
                             ;; query-params & form-params
                         (parameters/parameters-interceptor)
                             ;; content-negotiation
                         (muuntaja/format-negotiate-interceptor)
                             ;; encoding response body
                         (muuntaja/format-response-interceptor)
                             ;; exception handling
                         (exception/exception-interceptor
                          (merge
                           exception/default-handlers
                           {:reitit.coercion/request-coercion  (coercion-error-handler 400)
                            :reitit.coercion/response-coercion (coercion-error-handler 500)
                            clojure.lang.ExceptionInfo exception-info-handler}))
                             ;; decoding request body
                         (muuntaja/format-request-interceptor)
                             ;; coercing response bodys
                         (coercion/coerce-response-interceptor)
                             ;; coercing request parameters
                         (coercion/coerce-request-interceptor)
                             ;; multipart
                         (multipart/multipart-interceptor)
                             ;; read body after all and store in context
                         (read-raw-body)]}})

(defn router [routes]
  (pedestal/routing-interceptor
   (http/router routes router-settings)
    ;; optional default ring handler (if no routes have matched)
   (ring/routes
    (swagger-ui/create-swagger-ui-handler
     {:path "/"
      :config {:validatorUrl nil
               :operationsSorter "alpha"}})
    (ring/create-resource-handler)
    (ring/create-default-handler))))

(defrecord Router [router]
  component/Lifecycle
  (start [this] this)
  (stop  [this] this))

(defn new-router
  [routes]
  (map->Router {:router (router routes)}))
