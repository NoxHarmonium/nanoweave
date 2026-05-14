(ns ^{:doc "Syntax that represents manipulating text and strings."
      :author "Sean Dawson"}
 nanoweave.ast.text
  (:require [schema.core :as s :include-macros true]
            [nanoweave.ast.base :refer [Resolvable #?@(:cljs [AstSpan])]])
  #?(:clj (:import [nanoweave.ast.base AstSpan])))

(s/defrecord InterpolatedString [span :- AstSpan body :- [(s/protocol Resolvable)]])
(s/defrecord Regex [span :- AstSpan regex :- #?(:clj java.util.regex.Pattern :cljs js/RegExp)])
