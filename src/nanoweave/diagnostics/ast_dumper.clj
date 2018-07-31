(ns
  ^{:doc "AST dumper diagnostic tool.", :author "Sean Dawson"}
  nanoweave.diagnostics.ast-dumper
  (:use
    [rhizome.viz]
    [nanoweave.ast.base]
    [nanoweave.ast.literals])
  (:require
    [nanoweave.utils :refer [read-json-with-doubles]]
    [nanoweave.parser.parser :as parser]
    [clojure.pprint :as pp])
  (:import (nanoweave.ast.literals StringLit FloatLit BoolLit NilLit ArrayLit)))

(defn- primative-lit? [val]
  (or (instance? StringLit val)
      (instance? FloatLit val)
      (instance? BoolLit val)))

(defn- decend-map? [map]
  (not (primative-lit? map)))

(defn- decend? [val]
  (or (and (map? val) (decend-map? val)) (vector? val)))

(defn- decend [val]
  (if (map? val) (vals val) (seq val)))

(defn- describe-node [node]
  {:label (if (map? node) (cond
                            (primative-lit? node) (:value node)
                            :else (type node)
                            ) (type node))})

(defn- describe-edge [src, dest]
  {:label (cond (map? src) (first
                             (map (fn [[k _]] k)
                                  (filter (fn [[_ v]] (= v dest)) src))
                             )
                (vector? src)
                (.indexOf src dest))})

(defn- ast-map-to-graphviz [ast filename]
  (pp/pprint ast)
  (save-tree decend?
             decend
             ast
             :filename filename
             :node->descriptor describe-node
             :edge->descriptor describe-edge))

; No-op
(defn- process-ast [ast _] ast)

(defn dump-ast-as-graphviz [input-file output-file nweave-file]
  "Takes an input file and a nanoweave definition and outputs a graphical
representation of the AST in PNG format to the output file"
  (let [input (read-json-with-doubles (slurp input-file))
        nweave (slurp nweave-file)
        output (parser/transform input nweave process-ast)]
    (ast-map-to-graphviz output output-file)
    ; For some reason rhizome keeps the app open
    (System/exit 0)))
