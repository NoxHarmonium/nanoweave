(ns nanoweave.parser.ast
  (:require [schema.core :as s]))

(defprotocol Resolvable
  "Describes an AST node that can be resolved with an input.
  Used to transform AST trees into a final value."
  (resolve-value [this input] "Takes an input and resolves it to a file value."))

(s/defrecord InputLit [])
(s/defrecord IdentiferLit [value :- s/Str])
(s/defrecord StringLit [value :- s/Str])
(s/defrecord FloatLit [value :- s/Num])
(s/defrecord BoolLit [value :- s/Bool])
(s/defrecord NilLit [])
(s/defrecord DotOp [left :- Resolvable right :- Resolvable])
(s/defrecord ConcatOp [left :- Resolvable right :- Resolvable])
(s/defrecord AddOp [left :- Resolvable right :- Resolvable])
(s/defrecord SubOp [left :- Resolvable right :- Resolvable])
(s/defrecord MultOp [left :- Resolvable right :- Resolvable])
(s/defrecord DivOp [left :- Resolvable right :- Resolvable])
(s/defrecord ModOp [left :- Resolvable right :- Resolvable])
(s/defrecord NotOp [value :- Resolvable])
(s/defrecord NegOp [value :- Resolvable])

(defn safe-resolve-value [resolver input]
  (if (and (some? resolver)
           (satisfies? Resolvable resolver))
    (resolve-value resolver input)
    resolver))

(defn handle-bin-op [this input op]
  (op (safe-resolve-value (:left this) input)
       (safe-resolve-value (:right this) input)))

(extend-protocol Resolvable
  InputLit
  (resolve-value [_ input] input)
  IdentiferLit
  (resolve-value [this _] (:value this))
  String
  (resolve-value [this _] this)
  Double
  (resolve-value [this _] this)
  Long
  (resolve-value [this _] this)
  StringLit
  (resolve-value [this _] (:value this))
  FloatLit
  (resolve-value [this _] (:value this))
  BoolLit
  (resolve-value [this _] (:value this))
  NilLit
  (resolve-value [_ _] nil)
  DotOp
  (resolve-value [this input] (handle-bin-op this input get))
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
  NotOp
  (resolve-value [this input] (not (safe-resolve-value (:value this) input)))
  NegOp
  (resolve-value [this input] (- (safe-resolve-value (:value this) input))))
