(ns nanoweave.parser.ast
  "Defines the syntax that is parsed from the transform file.
  The tokens are parsed to first class records rather than just clojure functions
  so that in the future they can be extended with more features such as
  better error handling and type validation."
  (:require [schema.core :as s])
  (:use [nanoweave.parser.operators :only [xor]]))

(defprotocol Resolvable
  "Describes an AST node that can be resolved with an input.
  Used to transform AST trees into a final value."
  (resolve-value [this input] "Takes an input and resolves it to a file value."))

; Literals
(s/defrecord IdentiferLit [value :- s/Str])
(s/defrecord StringLit [value :- s/Str])
(s/defrecord FloatLit [value :- s/Num])
(s/defrecord BoolLit [value :- s/Bool])
(s/defrecord NilLit [])

(extend-protocol Resolvable
  IdentiferLit
  (resolve-value [this _] this)
  StringLit
  (resolve-value [this _] (:value this))
  FloatLit
  (resolve-value [this _] (:value this))
  BoolLit
  (resolve-value [this _] (:value this))
  NilLit
  (resolve-value [_ _] nil))

(defn safe-resolve-value [resolver input]
  "Resolves the input if the given resolver is not nil and conforms to Resolvable,
  otherwise it will just return the input."
  (if (and (some? resolver)
           (satisfies? Resolvable resolver))
    (resolve-value resolver input)
    resolver))

(defn handle-prop-access [this input]
  "A special case binary resolver.
  If the left side is an identifier then there is no existing value to access
  so it will get the value from scope. Otherwise it will just use the right
  side as a key to access the left side."
  (let [left (:left this)
        right (:right this)]
    (if (instance? IdentiferLit left)
      (get (get input (:value left)) (:value right))
      (get (safe-resolve-value left input) (:value right)))))

(defn handle-bin-op [this input op]
  "Binary resolver for generic binary operations."
  (op (safe-resolve-value (:left this) input)
      (safe-resolve-value (:right this) input)))


; Primitives
(extend-protocol Resolvable
  String
  (resolve-value [this _] this)
  Double
  (resolve-value [this _] this)
  Long
  (resolve-value [this _] this))

; Unary Operators

(s/defrecord NotOp [value :- Resolvable])
(s/defrecord NegOp [value :- Resolvable])

(extend-protocol Resolvable
  NotOp
  (resolve-value [this input] (not (safe-resolve-value (:value this) input)))
  NegOp
  (resolve-value [this input] (- (safe-resolve-value (:value this) input))))

; Binary Operators
(s/defrecord DotOp [left :- Resolvable right :- Resolvable])
(s/defrecord ConcatOp [left :- Resolvable right :- Resolvable])

(s/defrecord AddOp [left :- Resolvable right :- Resolvable])
(s/defrecord SubOp [left :- Resolvable right :- Resolvable])
(s/defrecord MultOp [left :- Resolvable right :- Resolvable])
(s/defrecord DivOp [left :- Resolvable right :- Resolvable])
(s/defrecord ModOp [left :- Resolvable right :- Resolvable])

(s/defrecord EqOp [left :- Resolvable right :- Resolvable])
(s/defrecord NotEqOp [left :- Resolvable right :- Resolvable])
(s/defrecord LessThanOp [left :- Resolvable right :- Resolvable])
(s/defrecord LessThanEqOp [left :- Resolvable right :- Resolvable])
(s/defrecord GrThanOp [left :- Resolvable right :- Resolvable])
(s/defrecord GrThanEqOp [left :- Resolvable right :- Resolvable])

(s/defrecord AndOp [left :- Resolvable right :- Resolvable])
(s/defrecord OrOp [left :- Resolvable right :- Resolvable])
(s/defrecord XorOp [left :- Resolvable right :- Resolvable])

(extend-protocol Resolvable
  DotOp
  (resolve-value [this input] (handle-prop-access this input))
  ConcatOp
  (resolve-value [this input] (handle-bin-op this input str))
  AddOp
  (resolve-value [this input] (handle-bin-op this input +))
  SubOp
  (resolve-value [this input] (handle-bin-op this input -))
  MultOp
  (resolve-value [this input] (handle-bin-op this input *))
  DivOp
  (resolve-value [this input] (handle-bin-op this input /))
  ModOp
  (resolve-value [this input] (handle-bin-op this input mod))
  EqOp
  (resolve-value [this input] (handle-bin-op this input =))
  NotEqOp
  (resolve-value [this input] (handle-bin-op this input not=))
  LessThanOp
  (resolve-value [this input] (handle-bin-op this input <))
  LessThanEqOp
  (resolve-value [this input] (handle-bin-op this input <=))
  GrThanOp
  (resolve-value [this input] (handle-bin-op this input >))
  GrThanEqOp
  (resolve-value [this input] (handle-bin-op this input >=))
  AndOp
  (resolve-value [this input] (handle-bin-op this input #(and %1 %2)))
  OrOp
  (resolve-value [this input] (handle-bin-op this input #(or %1 %2)))
  XorOp
  (resolve-value [this input] (handle-bin-op this input xor)))
