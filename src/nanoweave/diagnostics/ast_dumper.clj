(ns
 ^{:doc "AST dumper diagnostic tool.", :author "Sean Dawson"}
 nanoweave.diagnostics.ast-dumper
  (:require
   [rhizome.viz :as r]
   [nanoweave.utils :refer [read-json-with-doubles]]
   [nanoweave.transformers.file-transformer :as transformer]
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
                            :else (type node))
              (cond (instance? java.lang.String node) node
                    :else (type node)))})

(defn- describe-edge [src, dest]
  {:label (cond (map? src) (first
                            (map (fn [[k _]] k)
                                 (filter (fn [[_ v]] (= v dest)) src)))
                (vector? src)
                (.indexOf src dest))})

(defn- ast-map-to-graphviz [ast filename]
  (pp/pprint ast)
  (r/save-tree decend?
               decend
               ast
               :filename filename
               :node->descriptor describe-node
               :edge->descriptor describe-edge))

; No-op
(defn- process-ast [ast _] ast)

(defn dump-ast-as-graphviz
  "Takes an input file and a nanoweave definition and outputs a graphical
representation of the AST in PNG format to the output file"
  [input-file output-file nweave-file]
  (let [input (read-json-with-doubles (slurp input-file))
        nweave (slurp nweave-file)
        output (transformer/transform input nweave process-ast)]
    (ast-map-to-graphviz output output-file)
    ; For some reason rhizome keeps the app open
    (System/exit 0)))
