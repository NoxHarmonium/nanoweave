(ns jweave.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as string]
            [clojure.java.io :as io]
            [jweave.parser.parser :as parser])
  (:use clojure.pprint)
  (:gen-class))

(def cli-options
  [["-i" "--input PATH" "Path to input file"
    :validate [#(.exists (io/as-file %1)) "Input path must exist"]]
   ["-o" "--output PATH" "Path to output file"]
   ["-j" "--jweave PATH" "Path to jweave definition file"
    :validate [#(.exists (io/as-file %1)) "jweave definition path must exist"]]
   ["-v" nil "Verbosity level; may be specified multiple times to increase value"
    :id :verbosity
    :default 0
    :assoc-fn (fn [m k _] (update-in m [k] inc))]
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> ["Performs actions on an input file according to a given jweave definition file ."
        ""
        "Usage: jweave [options] transform"
        ""
        "Options:"
        options-summary
        ""
        "Actions:"
        "  transform\tTransforms the given input file"
        ""]
       (string/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn validate-args
  "Validate command line arguments. Either return a map indicating the program
  should exit (with a error message, and optional ok status), or a map
  indicating the action the program should take and the options provided."
  [args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options) ; help => exit OK with usage summary
      {:exit-message (usage summary) :ok? true}
      errors ; errors => exit with description of errors
      {:exit-message (error-msg errors)}
      ;; custom validation on arguments
      (and (= 1 (count arguments))
           (#{"transform"} (first arguments)))
      {:action (first arguments) :options options}
      :else ; failed custom validation => exit with usage summary
      {:exit-message (usage summary)})))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn -main [& args]
  (let [{:keys [action options exit-message ok?]} (validate-args args)]
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (case action
        "transform"  (parser/transform (:input options) (:output options) (:jweave options))
        ))))