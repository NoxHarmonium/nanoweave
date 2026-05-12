(ns
 ^{:doc "Functions to help with resolver errors", :author "Sean Dawson"}
 nanoweave.resolvers.errors
  (:require [nanoweave.ast.base :refer [->ErrorWithContext]])
  (:import [clojure.lang IExceptionInfo]
           [nanoweave.ast.base ErrorWithContext]))

(defn unwrap-resolve-error
  "If ex is a valid resolve error, will unwrap the error data,
   otherwise it will return nil"
  [ex]
  (when (instance? IExceptionInfo ex)
    (when-let [error-data (ex-data ex)]
      (when (instance? ErrorWithContext error-data)
        error-data))))

(defn resolve-error?
  "Returns true if the error was thown by throw-resolve error,
   otherwise false"
  [ex]
  (when-let [error-data (unwrap-resolve-error ex)]
    (let [{:keys [type]} error-data]
      (= type :resolve-error))))

(defn throw-resolve-error
  "Throws an error message that can be shown when an error occurs in
   the resolution phase."
  [message ast-node & [cause]]
  (throw (ex-info message (->ErrorWithContext message :resolve-error ast-node (:span ast-node) cause nil) cause)))