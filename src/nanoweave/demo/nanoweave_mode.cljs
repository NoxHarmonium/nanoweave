(ns nanoweave.demo.nanoweave-mode
  (:require ["@codemirror/language" :refer [StreamLanguage]]))

(def ^:private keywords
  #{"let" "when" "else" "typeof" "match" "import"
    "map" "filter" "reduce" "rmatch" "rfind" "rsplit"
    "is" "as" "to" "until"})

(def ^:private builtins
  #{"true" "false" "nil"})

(def ^:private token
  (fn [^js stream _state]
    (cond
      ;; Line comment
      (.match stream "//")
      (do (.skipToEnd stream) "comment")

      ;; String literal
      (= (.next stream) "\"")
      (loop []
        (let [ch (.next stream)]
          (cond
            (nil? ch) "string"
            (= ch "\\") (do (.next stream) (recur))
            (= ch "\"") "string"
            :else (recur))))

      ;; Number literal
      (.match stream (js/RegExp "^[0-9]+(\\.[0-9]+)?"))
      "number"

      ;; Identifiers and keywords
      (.match stream (js/RegExp "^[a-zA-Z_][a-zA-Z0-9_]*"))
      (let [word (.current stream)]
        (cond
          (keywords word) "keyword"
          (builtins word) "atom"
          :else "variable"))

      ;; Lambda param %1, %2, ...
      (.match stream (js/RegExp "^%[0-9]*"))
      "variableName.special"

      ;; Operators
      (.match stream (js/RegExp "^(->|\\+\\+|<=|>=|==|!=|&&|\\|\\||[+\\-*/%<>!#^.])"))
      "operator"

      ;; Skip other chars
      :else
      (do (.next stream) nil))))

(def nanoweave-language
  (.define StreamLanguage
           #js {:token token}))
