(ns ring.middleware.header-propagation
  (:require [ring.middleware.client :as ring-client]))

(defn- build-client-wrap-header-propagation [headers]
  (fn [client]
    (fn [request]
      (let [new-headers (merge headers (request :headers))]
        (client (assoc request :headers new-headers))))))

(defn- select-headers [request header-names]
  (select-keys (request :headers) header-names))

(defn wrap-header-propagation
  "Propagates headers identified by `header-names` to clj-http calls."
  [handler header-names]
  (ring-client/wrap-client-middleware-builder
   handler
   (fn [request]
     (-> request
         (select-headers header-names)
         (build-client-wrap-header-propagation)))))

(def istio-header-names
  "The header names required by Istio to propagate trace contexts.

  See: https://istio.io/docs/tasks/telemetry/distributed-tracing/overview"
  #{"x-request-id"
    "x-b3-traceid"
    "x-b3-spanid"
    "x-b3-parentspanid"
    "x-b3-sampled"
    "x-b3-flags"
    "x-ot-span-context"})

(def b3-header-names
  "The header names from the B3 Propagation spec to propagate trace contexts.

  See: https://github.com/openzipkin/b3-propagation#http-encodings"
  #{"x-b3-traceid"
    "x-b3-spanid"
    "x-b3-parentspanid"
    "x-b3-sampled"
    "x-b3-flags"
    "b3"})
