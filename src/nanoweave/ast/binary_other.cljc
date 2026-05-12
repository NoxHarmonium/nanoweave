(ns ^{:doc "Syntax that represents miscellaneous operations that can be done on two expressions."
      :author "Sean Dawson"}
 nanoweave.ast.binary-other
  (:require [schema.core :as s :include-macros true]
            [nanoweave.ast.base :refer [Resolvable #?@(:cljs [AstSpan])]])
  #?(:clj (:import [nanoweave.ast.base AstSpan])))

(s/defrecord DotOp [span :- AstSpan left :- Resolvable right :- Resolvable])
(s/defrecord ConcatOp [span :- AstSpan left :- Resolvable right :- Resolvable])
(s/defrecord OpenRangeOp [span :- AstSpan left :- Resolvable right :- Resolvable])
(s/defrecord ClosedRangeOp [span :- AstSpan left :- Resolvable right :- Resolvable])
(s/defrecord IsOp [span :- AstSpan left :- Resolvable right :- Resolvable])
(s/defrecord AsOp [span :- AstSpan left :- Resolvable right :- Resolvable])
