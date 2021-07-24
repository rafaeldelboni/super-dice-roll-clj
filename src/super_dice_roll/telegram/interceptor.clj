(ns super-dice-roll.telegram.interceptor)

(defn verification-interceptor []
  {:name ::validate-request-interaction
   :enter (fn [ctx]
            (let [request (:request ctx)
                  {{:keys [config]} :components} request
                  bot-token-request (get-in ctx [:request :parameters :path :bot-token])
                  bot-token (get-in config [:config :telegram :bot-token])]
              (if (= bot-token-request bot-token)
                ctx
                (assoc ctx :response {:headers {"Content-Type" "application/text"}
                                      :status 401
                                      :body "invalid bot-token sent"}))))})
