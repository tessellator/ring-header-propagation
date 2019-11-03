(ns ring.middleware.header-propagation-test
  (:require [clj-http.client :as http]
            [clj-http.fake :as fake]
            [clojure.test :refer :all]
            [ring.middleware.header-propagation :refer [wrap-header-propagation]]
            [ring.mock.request :as mock]))

(def fake-routes
  {"http://example.com/"
   (fn [req] {:status 200
              :headers {}
              :body (str (req :headers))})})

(defn call-client-handler [req]
  {:status 200
   :headers {}
   :body (:body (http/get "http://example.com/" {:headers {"foo" "bar"}}))})

(defn add-headers [mock-request headers]
  (reduce (fn [req [header val]]
            (mock/header req header val))
          mock-request
          headers))

(defn select-headers [request header-names]
  (select-keys (request :headers) header-names))

(defn parse-headers-in-body [response header-names]
  (let [parsed-body (read-string (response :body))]
    (select-keys parsed-body header-names)))

(deftest test-wrap-header-propagation
  (fake/with-fake-routes-in-isolation fake-routes
    (testing "happy path"
      (let [headers {"x-session-id" "1234"
                     "x-request-id" "1"}
            handler (wrap-header-propagation call-client-handler (keys headers))
            request (add-headers (mock/request :get "/") headers)
            response (handler request)
            received-headers (parse-headers-in-body response (keys headers))]
        (is (= headers received-headers))))

    (testing "existing headers are not overwritten"
      (let [headers {"x-session-id" "1234"
                     "x-request-id" "1"
                     "foo" "xyz"}
            handler (wrap-header-propagation call-client-handler (keys headers))
            request (add-headers (mock/request :get "/") headers)
            response (handler request)
            received-headers (parse-headers-in-body response (keys headers))]
        (is (= {"x-session-id" "1234"
                "x-request-id" "1"
                "foo" "bar"}
               received-headers))))))
