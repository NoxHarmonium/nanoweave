(ns
 ^{:doc "Functions to help with resolver errors", :author "Sean Dawson"}
 nanoweave.resolvers.errors
  (:require [nanoweave.utils :refer [format-code-frame-span]])
  (:import [clojure.lang IExceptionInfo]))

(defn format-resolution-error-as-code-frame
  "Formats AST resolution error messages by showing it in context."
  [ast-node message input]
  (let [span (:span ast-node)
        pos (:start span)
        src (let [l (:src ast-node)] (if (empty? l) "" (str l " ")))
        ln (:line pos)
        col (:col pos)
        preamble (format "Compliation failure: (ln: %d col: %d src: %s)\n"
                         ln col src)]
    (format-code-frame-span span input preamble message)))

(defn resolve-error?
  "Returns true if the error was thown by throw-resolve error,
   otherwise false"
  [ex]
  (if (instance? IExceptionInfo ex)
    (let [{:keys [type]} (ex-data ex)]
      (= type :resolve-error))
    false))

(defn throw-resolve-error
  "Throws an error message that can be shown when an error occurs in
   the resolution phase."
  [ast-node message]
  (throw (ex-info message {:type :resolve-error :ast-node ast-node})))
