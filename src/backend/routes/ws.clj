(ns backend.routes.ws
  (:require
   [compojure.core :as compojure :refer [GET POST]]
   [mount.core :refer [defstate]]
   [taoensso.sente :as sente]
   [taoensso.sente.server-adapters.aleph :refer [get-sch-adapter]]))

(defstate server-chsk
  :start (sente/make-channel-socket-server!
          (get-sch-adapter)
          {}))

(def get-chsk
  (GET "/chsk" req
    ((:ajax-get-or-ws-handshake-fn server-chsk) req)))

(def post-chsk
  (POST "/chsk" req
    ((:ajax-post-fn server-chsk) req)))

(comment
  ;; socket payload example
  {:aleph/request-arrived 9018227397287
   :aleph/keep-alive?     true
   :cookies               {"ring-session" {:value "dbc36952-56cf-4326-b7f1-5f04e2124a48"}}
   :remote-addr           "127.0.0.1"
   :params                {:udt        "1625854625311"
                           :client-id  "a67fed90-6bf9-4e1d-8ff3-3cdfb5685dc6"
                           :handshake? "true"}
   :route-params          {}
   :headers               {"sec-fetch-site"   "same-origin"
                           "x-requested-with" "XMLHTTPRequest"
                           "sec-ch-ua-mobile" "?0"
                           "host"             "localhost:3000"
                           "user-agent"       "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36"
                           "cookie"           "ring-session=dbc36952-56cf-4326-b7f1-5f04e2124a48"
                           "sec-ch-ua"        "\"Chromium\";v=\"91\", \" Not;A Brand\";v=\"99\""
                           "referer"          "http://localhost:3000/"
                           "connection"       "keep-alive"
                           "x-csrf-token"     "k8xUqdaZbmogDhy969amG7mInelmvWbArY8U4AGA9jBo3x1BkdgyyZ+tmh6bXxCmLQVsSK9Bk/QprcWs"
                           "accept"           "*/*"
                           "accept-language"  "en-US,en;q=0.9,fr;q=0.8"
                           "sec-fetch-dest"   "empty"
                           "accept-encoding"  "gzip, deflate, br"
                           "sec-fetch-mode"   "cors"
                           "dnt"              "1"
                           "sec-gpc"          "1"}
   :server-port           3000
   :form-params           {}
   :compojure/route       [:get "/chsk"]
   :session/key           nil
   :query-params          {"udt"        "1625854625311"
                           "client-id"  "a67fed90-6bf9-4e1d-8ff3-3cdfb5685dc6"
                           "handshake?" "true"}
   :uri                   "/chsk"
   :server-name           "localhost"
   :anti-forgery-token    "u+pq+hTortOPW6KlKccf36WZ3nN7jIj5UfbFiMACGJZfOEBhjHZc2VXOUJM4fYjebJLLNYjbfDV8T5lU"
   :query-string          "udt=1625854625311&client-id=a67fed90-6bf9-4e1d-8ff3-3cdfb5685dc6&handshake%3F=true"
   :body                  nil
   :scheme                :http
   :request-method        :get
   :session               {}})
