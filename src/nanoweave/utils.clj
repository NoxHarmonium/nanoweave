(ns nanoweave.utils
  (:require [clojure.walk :refer [prewalk]]
            [cheshire.core :refer [parse-string]]
            [clojure.string :refer [split-lines join]]))

(defn read-json-with-doubles
  "Reads JSON but makes sure that numbers are read as doubles.
   In nanoweave we currently deal with all numbers as doubles,
   like in Javascript. This may change in the future but keeps
   it simple for now."
  [string]
  (let [json-map (parse-string string)]
    (prewalk #(if (number? %1) (double %1) %1) json-map)))

(defn convert-to-number
  "You can coerce strings or other number types in to a Nanoweave Number type/
   This makes sure that the conversion is done in a 'smart' way.
   It will try very hard to do the conversion, even converting it to a string first,
   but it will still blow up if the input doesn't make sense"
  [anything]
  (cond
    (number? anything) (double anything)
    (string? anything) (Double/parseDouble anything)
    (boolean? anything) (if anything 1.0 0.0)
    (nil? anything) anything
    :else (try
            (Double/parseDouble (str anything))
            (catch Exception _ (str "Cannot convert type '" (type anything) "' to a number.")))))

(defn safe-type
  "Does the same thing as the built-in Clojure type function,
   but will return 'Nil' instead of an empty string if it is passed
   a nil value"
  [anything]
  (if (nil? anything) "Nil" (str (type anything))))

; From https://github.com/Prismatic/plumbing/
(defn map-vals
  "Build map k -> (f v) for [k v] in map, preserving the initial type"
  [f m]
  (cond
    (sorted? m)
    (reduce-kv (fn [out-m k v] (assoc out-m k (f v))) (sorted-map) m)
    (map? m)
    (persistent! (reduce-kv (fn [out-m k v] (assoc! out-m k (f v))) (transient {}) m))
    :else nil))

(defn dynamically-load-class
  "Loads a Java class into the current namespace and returns it"
  [class-name]
  (.importClass (the-ns *ns*)
                (clojure.lang.RT/classForName class-name))
  (clojure.lang.RT/classForName class-name))

; Thanks: https://stackoverflow.com/a/27914262

(defn contains-many?
  "Checks if a map m contains all the keys in sequence ks"
  [m ks]
  (every? #(contains? m %) ks))

; Thanks: https://stackoverflow.com/a/20054111

(defmacro declare-extern
  [& syms]
  (let [n (ns-name *ns*)]
    `(do
       ~@(for [s syms]
           `(do
              (ns ~(symbol (namespace s)))
              (declare ~(symbol (name s)))))
       (in-ns '~n))))

; Thanks: https://stackoverflow.com/a/4831170/1153203

(defn find-thing [needle haystack]
  (keep-indexed #(when (= %2 needle) %1) haystack))

;; Code Frame
; Inspired by https://babeljs.io/docs/en/babel-code-frame

(def eol (System/getProperty "line.separator"))
(def code-frame-options {:lines-above 2
                         :lines-below 3
                         :gutter "| "})

(defn format-line
  "Formats a line of code for a code frame"
  [gutter lines line-number]
  ; Line number is one based, the lines array is zero based 
  ; So we need to subtract one from the line number
  (str line-number gutter (get lines (dec line-number))))

(defn format-code-lines
  "Formats a range of lines of code for a code frame"
  [lines min-line max-line gutter]
  (join eol (map (partial format-line gutter lines)
                 (range min-line max-line))))

(defn format-pointer
  "Formats the arrow that points at the target line of code to indicate the column the issue occurred"
  [gutter-length col]
  (str (join (repeat (+ col gutter-length) " ")) "^ "))

(defn format-highlight-cols
  "Formats the squiggles that highlight a range of text on a line to indicate which columns the issue occurred"
  [gutter-length start-col end-col]
  (str
   (join (repeat (+ start-col gutter-length) " "))
   (join (repeat (inc (- end-col start-col)) "~"))))

(defn format-hightlight-lines
  "Formats a range of lines of code for a code frame"
  [lines min-line max-line min-col max-col gutter]
  (let [gutter-length (count gutter)
        line-range (range min-line (inc max-line))
        formatted-lines
        (map (fn [line-num]
               (let [line (get lines (dec line-num))
                     line-length (count line)]
                 [(format-line gutter lines line-num)
                  ; This strangeness is to support spans that go over a single line
                  (cond
                     ; Single line - just highlight from min -> max column numbers
                    (= min-line max-line) (format-highlight-cols gutter-length min-col max-col)
                    ; Multi line
                    ; the highlighting on the first line goes from the start of the span to the end of the line 
                    (= line-num min-line) (format-highlight-cols gutter-length min-col line-length)
                    ; The highlighting on the last line goes from the start of the line to the end of the span
                    (= line-num max-line) (format-highlight-cols gutter-length 1 max-col)
                    ; the lhighlighting for the lines in the middle span the entire line because they are in the middle of the multi line span
                    :else (format-highlight-cols gutter-length 1 line-length))])) line-range)]
    (join eol (flatten formatted-lines))))

(defn format-code-frame
  "Formats parsing error messages by showing it in context."
  [ln col input preamble message]
  (if input
    (let [{:keys [gutter lines-below lines-above]} code-frame-options
          lines (split-lines input)
          min-line (max (- ln lines-above) 1)
          max-line (min (+ ln lines-below) (count lines))
          text-before (format-code-lines lines min-line ln gutter)
          target-line (format-line gutter lines ln)
          text-after (format-code-lines lines (inc ln) max-line gutter)
          pointer (format-pointer (count gutter) col)]
      (str
       preamble
       text-before
       eol
       target-line
       eol
       pointer
       message
       eol
       text-after
       eol))
    (str
     preamble
     eol
     message
     eol)))

(defn format-code-frame-span
  "Formats AST error messages by showing it in context."
  [span input preamble message]
  (if input
    (let [{:keys [gutter lines-below lines-above]} code-frame-options
          {:keys [start end]} span
          start-line (:line start)
          end-line (:line end)
          start-col (:col start)
          end-col (:col end)
          lines (split-lines input)
          min-line (max (- start-line lines-above) 1)
          max-line (min (+ end-line lines-below) (count lines))
          text-before (format-code-lines lines min-line start-line gutter)
          text-after (format-code-lines lines (inc end-line) max-line gutter)]
      (str
       preamble
       text-before
       eol
       (format-hightlight-lines lines start-line end-line start-col end-col gutter)
       eol
       message
       eol
       text-after
       eol))
    (str
     preamble
     eol
     message
     eol)))

(defn format-error-with-context
  "Takes a ErrorWithContext record and formats it for printing to a console."
  ; TODO: Use more keys of the error type
  ; type - we should switch on this to show the right type of error (e.g. read/write/parse etc.)
  ; ast-node - could use this for some smart error reporting
  ; cause - could print this if it exists for more info
  [{:keys [message span input]}]
  (let [{:keys [start end]} span
        {:keys [line col src]} start
        preamble (format "Compliation failure: (ln: %d col: %d src: %s)\n"
                         line col src)]
    (if (= start end)
      (format-code-frame line col input preamble message)
      (format-code-frame-span span input preamble message))))