(ns ^{:doc "Support macros and functions for monads", :author "Sean Dawson"}
 nanoweave.monadic.base
  (:require [clojure.tools.macro :refer [with-symbol-macros]]))

; Thanks Konrad Hinsen (clojure.algo.monads)
; I took some stuff from that repo and simplified it for here

(defmacro monad
  "Define a monad by defining the monad operations. The definitions
   are written like bindings to the monad operations m-bind and
   m-return."
  [operations]
  `(let [~'m-bind ::this-monad-does-not-define-m-bind
         ~'m-return ::this-monad-does-not-define-m-return
         ~@operations]
     {:m-return ~'m-return
      :m-bind ~'m-bind}))

(defmacro defmonad
  "Define a named monad by defining the monad operations. The definitions
   are written like bindings to the monad operations m-bind and
   m-result."

  ([name doc-string operations]
   (let [doc-name (with-meta name {:doc doc-string})]
     `(defmonad ~doc-name ~operations)))

  ([name operations]
   `(def ~name (monad ~operations))))

(defmacro with-monad
  "Evaluates an expression after replacing the keywords defining the
   monad operations by the functions associated with these keywords
   in the monad definition given by name."
  [monad & exprs]
  `(let [name# ~monad
         ~'m-bind (:m-bind name#)
         ~'m-result (:m-result name#)]
     (with-symbol-macros ~@exprs)))

(defmacro domonad
  "Similar to the 'do' notation in Haskell or for comprehensions in Scala.
   
   Expands into nested bind forms and a function body. The pattern:

   (m-bind p1 (fn [v1]
   (m-bind p2 (fn [v2]
   ...
     (return (f v1 v2 ...))))))

   can be more conveniently be written as:

   (domonad [v1 p1 v2 p2 ...] (f v1 v2 ...))"
  [monad [& bindings] & body]
  (let [[sym p] (take 2 bindings)]
    (if (= 2 (count bindings))
      ; TODO: Use with-monad instead of manual destructuring
      `((:m-bind ~monad) ~p (fn [~sym] ~@body))
      `((:m-bind ~monad) ~p (fn [~sym] (domonad ~monad ~(drop 2 bindings) ~@body))))))

