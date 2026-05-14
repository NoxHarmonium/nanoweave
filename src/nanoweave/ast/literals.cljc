(ns ^{:doc "Syntax that represents literal values."
      :author "Sean Dawson"}
 nanoweave.ast.literals
  (:require [schema.core :as s :include-macros true]
            [nanoweave.ast.base :refer [Resolvable #?@(:cljs [AstSpan])]])
  #?(:clj (:import [nanoweave.ast.base AstSpan])))

(s/defrecord IdentiferLit [span :- AstSpan value :- s/Str static-prefix :- s/Bool])
(s/defrecord StringLit [span :- AstSpan value :- s/Str])
(s/defrecord FloatLit [span :- AstSpan value :- s/Num])
(s/defrecord BoolLit [span :- AstSpan value :- s/Bool])
(s/defrecord NilLit [span :- AstSpan])
(s/defrecord ArrayLit [span :- AstSpan value :- [(s/protocol Resolvable)]])
(s/defrecord TypeLit [span :- AstSpan value :- (s/enum "Number" "String" "Boolean" "Nil" "Array")])
(s/defrecord PairLit [span :- AstSpan key :- (s/protocol Resolvable) value :- (s/protocol Resolvable)])
(s/defrecord ObjectLit [span :- AstSpan pairs :- [(s/protocol Resolvable)]])
