(ns ^{:doc "Syntax that represents manipulating text and strings."
      :author "Sean Dawson"}
 nanoweave.ast.text
  (:require [schema.core :as s]
            [nanoweave.ast.base :refer [Resolvable]]))

(s/defrecord InterpolatedString [body :- [Resolvable]])
(s/defrecord Regex [regex :- [java.util.regex.Pattern]])
