# ring-header-propagation

Propagate headers received through calls using
[clj-http](https://github.com/dakrone/clj-http).

NOTE: Because the internals depend on thread-local bindings, this middleware
cannot be used with async ring handlers.

## Usage

```clojure
(require '[ring.middleware.header-propagation :refer [wrap-header-propagation]])

(def handler
  (-> my-routes
      (wrap-header-propagation #{"x-session-id"})))
```

For convenience, this library provides collections of headers names required by
Istio and the B3 Propagation specification.

## License

Copyright © 2019 Thomas C. Taylor

Distributed under the Eclipse Public License version 2.0.
