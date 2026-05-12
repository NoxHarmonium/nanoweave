(ns nanoweave.utils
  #?(:cljs (:require [clojure.walk :refer [prewalk]]
                     [clojure.string :refer [split-lines join]])))

#?(:cljs
   (do
     (defn read-json-with-doubles
       "Reads JSON but makes sure that numbers are read as doubles.
        In nanoweave we currently deal with all numbers as doubles,
        like in Javascript."
       [string]
       (let [json-map (js->clj (js/JSON.parse string))]
         (prewalk #(if (number? %1) (double %1) %1) json-map)))

     (defn convert-to-number
       "Coerces strings or other number types into a Nanoweave Number type."
       [anything]
       (cond
         (number? anything) (double anything)
         (string? anything) (js/parseFloat anything)
         (boolean? anything) (if anything 1.0 0.0)
         (nil? anything) anything
         :else (let [n (js/parseFloat (str anything))]
                 (if (js/isNaN n)
                   (str "Cannot convert type '" (type anything) "' to a number.")
                   n))))

     (defn safe-type
       "Returns the type of a value as a string, or 'Nil' for nil."
       [anything]
       (if (nil? anything) "Nil" (str (type anything))))

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
       "Not supported in ClojureScript."
       [class-name]
       (js/console.warn "dynamically-load-class not supported in ClojureScript:" class-name)
       nil)

     (defn contains-many?
       "Checks if a map m contains all the keys in sequence ks"
       [m ks]
       (every? #(contains? m %) ks))

     (defn find-thing [needle haystack]
       (keep-indexed #(when (= %2 needle) %1) haystack))

     (def eol "\n")
     (def code-frame-options {:lines-above 2
                              :lines-below 3
                              :gutter "| "})

     (defn format-line [gutter lines line-number]
       (str line-number gutter (get lines (dec line-number))))

     (defn format-code-lines [lines min-line max-line gutter]
       (join eol (map (partial format-line gutter lines)
                      (range min-line max-line))))

     (defn format-pointer [gutter-length col]
       (str (join (repeat (+ col gutter-length) " ")) "^ "))

     (defn format-highlight-cols [gutter-length start-col end-col]
       (str
        (join (repeat (+ start-col gutter-length) " "))
        (join (repeat (inc (- end-col start-col)) "~"))))

     (defn format-hightlight-lines [lines min-line max-line min-col max-col gutter]
       (let [gutter-length (count gutter)
             line-range (range min-line (inc max-line))
             formatted-lines
             (map (fn [line-num]
                    (let [line (get lines (dec line-num))
                          line-length (count line)]
                      [(format-line gutter lines line-num)
                       (cond
                         (= min-line max-line) (format-highlight-cols gutter-length min-col max-col)
                         (= line-num min-line) (format-highlight-cols gutter-length min-col line-length)
                         (= line-num max-line) (format-highlight-cols gutter-length 1 max-col)
                         :else (format-highlight-cols gutter-length 1 line-length))])) line-range)]
         (join eol (flatten formatted-lines))))

     (defn format-code-frame [ln col input preamble message]
       (if input
         (let [{:keys [gutter lines-below lines-above]} code-frame-options
               lines (split-lines input)
               min-line (max (- ln lines-above) 1)
               max-line (min (+ ln lines-below) (count lines))
               text-before (format-code-lines lines min-line ln gutter)
               target-line (format-line gutter lines ln)
               text-after (format-code-lines lines (inc ln) max-line gutter)
               pointer (format-pointer (count gutter) col)]
           (str preamble text-before eol target-line eol pointer message eol text-after eol))
         (str preamble eol message eol)))

     (defn format-code-frame-span [span input preamble message]
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
           (str preamble text-before eol
                (format-hightlight-lines lines start-line end-line start-col end-col gutter)
                eol text-after eol))
         (str preamble eol message eol)))))
