(ns ^{:doc "Syntax that represents manipulating text and strings."
      :author "Sean Dawson"}
 nanoweave.ast.text
  (:require [schema.core :as s]
            [nanoweave.ast.base :refer [Resolvable]])
  (:import [nanoweave.ast.base AstSpan]))

(s/defrecord InterpolatedString [span :- AstSpan body :- [Resolvable]])
(s/defrecord Regex [span :- AstSpan regex :- [java.util.regex.Pattern]])
