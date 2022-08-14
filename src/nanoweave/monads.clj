(ns ^{:doc "Monadic style functional operators, based on the Kern library.
            Note: I don't really know what I'm doing here. I'm mostly reverse engineering other libraries
            and trying to remember my experience with Haskell.", :author "Sean Dawson"}
 nanoweave.monads)

(defn ok
  "Wraps a value in an OK (right) type."
  [value]
  {:ok true :value value})
(defn err
  "Wraps a value in an Err (left) type."
  [error] {:ok false :error error})

(defn chain
  "Lifts the value of an either into function f if the it is an Ok.
   Otherwise it will short circuit f.
   
   Function f must return an either value which will replace the current value.

   Also known as flat map in other languages."
  [f]
  (fn [e]
    (if (:ok e)
      (f (:value e))
      e)))

(defn chain*
  "If the previous value was Ok, will set val as the new monad.
   
   This is useful when you aren't passing state along."
  [val]
  (fn [e]
    (if (:ok e)
      val
      e)))

;; TODO: This could probably be made more generic with a 'pure' function
(defn run [f & val] (f (ok (first val))))

(defn return
  "Used to finish a bind statement"
  [_]
  (chain (fn [value] (ok value))))

(defn map-error
  "If e is an Err, apply function f to the wrapped error, otherwise pass through unchanged"
  [f e]
  (if (:ok e)
    e
    (err (f (:error e)))))

;; TODO: Rework all these doc strings. I'm sure I've got all the terminology wrong
;; but I can't check since I don't have internet here
(defn >>=
  "Binds monad tranformer m to function f which gets p's value and returns
   a new monad. Function m must define a single parameter. The
   argument it receives is the value returned by m, not ms' return
   value, which is a monad."
  [m f]
  (fn [s]
    (let [s1 (m s)]
      (if (:ok s1)
        (let [s2 ((f (:value s1)) s1)]
          s2)
        s1))))

(defmacro bind
  "Expands into nested >>= forms and a function body. The pattern:

   (>>= p1 (fn [v1]
   (>>= p2 (fn [v2]
   ...
     (return (f v1 v2 ...))))))

   can be more conveniently be written as:

   (bind [v1 p1 v2 p2 ...] (return (f v1 v2 ...)))"
  [[& bindings] & body]
  (let [[sym p] (take 2 bindings)]
    (if (= 2 (count bindings))
      `(>>= ~p (fn [~sym] ~@body))
      `(>>= ~p (fn [~sym] (bind ~(drop 2 bindings) ~@body))))))
