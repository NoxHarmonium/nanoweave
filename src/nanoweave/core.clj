(ns nanoweave.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as string]
            [clojure.java.io :as io]
            [nanoweave.utils :refer [format-error-with-context]]
            [nanoweave.transformers.file-transformer :as transformer]
            [nanoweave.diagnostics.ast-dumper :as dumper])
  (:gen-class))

(def cli-options
  [["-i" "--input PATH" "Path to input file" :validate
    [#(.exists (io/as-file %1)) "Input path must exist"]]
   ["-o" "--output PATH" "Path to output file"]
   ["-n" "--nweave PATH" "Path to nanoweave definition file" :validate
    [#(.exists (io/as-file %1)) "nweave definition path must exist"]]
   ["-v" nil
    "Verbosity level; may be specified multiple times to increase value" :id
    :verbosity :default 0 :assoc-fn (fn [m k _] (update-in m [k] inc))]
   ["-h" "--help"]])

(defn usage
  [options-summary]
  (->>
   ["Performs actions on an input file according to a given nanoweave definition file."
    "" "Usage: nanoweave [options] transform" "" "Options:" options-summary ""
    "Actions:" "  transform\tTransforms the given input file"
    "dump-ast\tDumps the nweave AST to a file" ""]
   (string/join \newline)))

(defn error-msg
  [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn validate-args
  "Validate command line arguments. Either return a map indicating the program
  should exit (with a error message, and optional ok status), or a map
  indicating the action the program should take and the options provided."
  [args]
  (let [{:keys [options arguments errors summary]}
        (parse-opts args cli-options)]
    (cond
      (:help options) ; help => exit OK with usage summary
      {:exit-message (usage summary), :ok? true}
      errors ; errors => exit with description of errors
      {:exit-message (error-msg errors)}
      ;; custom validation on arguments
      (and (= 1 (count arguments)) (#{"transform" "dump-ast"} (first arguments)))
      {:action (first arguments), :options options}
      :else ; failed custom validation => exit with usage summary
      {:exit-message (usage summary)})))

(defn exit [status msg] (println msg) (System/exit status))

(defn exit-with-error-when-not-ok!
  "If the transform result is not ok, will print a human friendly explanation,
   otherwise it will do nothing"
  [result]
  (when (not (:ok result))
    (let [message (if-let [contextual-error (:error result)]
                    (format-error-with-context contextual-error)
                    "An unknown error occurred")]
      (exit 1 message))))

(defn -main
  [& args]
  (let [{:keys [action options exit-message ok?]} (validate-args args)]
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (case action
        "transform" (exit-with-error-when-not-ok!
                     (transformer/transform-files! (:input options)
                                                   (:output options)
                                                   (:nweave options)))
        "dump-ast" (dumper/dump-ast-as-graphviz (:input options)
                                                (:output options)
                                                (:nweave options))))))
