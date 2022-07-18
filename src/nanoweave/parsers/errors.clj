(ns
 ^{:doc "Functions to help with parser errors", :author "Sean Dawson"}
 nanoweave.parsers.errors
  (:require [blancas.kern.core :refer [def-]]
            [blancas.kern.i18n :refer [i18n fmt]]
            [clojure.string :refer [join]]
            [nanoweave.utils :refer [format-code-frame]]))

; Copied from https://github.com/blancas/kern/blob/master/src/main/clojure/blancas/kern/core.clj
; because the methods were not made public

;; Error types.
(def- err-system 0) ;; Used in satisfy for specific unexpected input.
(def- err-unexpect 1) ;; Used on any unexpected input to show a message.
(def- err-expect 2) ;; Used to show a message of what's expected.
(def- err-message 3) ;; Used for any kind of message from client code.

(defn- get-msg
  "Get the text from message types system, unexpect, and message."
  [pmsg]
  (let [type (:type pmsg)
        text (-> pmsg :text force)]
    (cond (= type err-system) (fmt :unexpected text)
          (= type err-unexpect) (fmt :unexpected text)
          (= type err-message) text)))

(defn- get-msg-expect
  "Get the text from a list of messages of type expect."
  [lst]
  (let [show (fn [xs]
               (let [comma-sep (join (i18n :comma) (butlast xs))
                     or-last (fmt :or (last xs))]
                 (str comma-sep or-last)))
        opts (map (comp force :text) lst)
        cnt (count opts)]
    (fmt :expecting (if (= cnt 1) (first opts) (show opts)))))

(defn- get-msg-list
  "Gets the text of error messages as a list."
  [{msgs :msgs}]
  (let [ms (distinct msgs)]
    (concat
    ;; TODO: Unexpected token messages are probably useful,
    ;; but at the moment I can't work out how to separate
    ;; them out in meaningful way at the moment
     (let [lst (filter #(contains? #{err-system err-unexpect} (:type %)) ms)]
       (take 1 (reduce #(conj %1 (get-msg %2)) [] lst)))
     ;;
     (let [lst (filter #(= (:type %) err-expect) ms)]
       (if (empty? lst) lst (list (get-msg-expect lst))))
     (let [lst (filter #(= (:type %) err-message) ms)]
       (reduce #(conj %1 (get-msg %2)) [] lst)))))

(defn format-parsing-error-as-code-frame
  "Formats a parsing error messages by showing it in context."
  [pstate input]
  (assert (not (:ok pstate)) "Provided parser state does not indicate an error")
  (let [err (:error pstate)
        pos (:pos err)
        src (let [l (:src pos)] (if (empty? l) "" (str l " ")))
        ln (:line pos)
        col (:col pos)
        preamble (str "Parsing error: "
                      (format (i18n :err-pos) src ln col))
        message (join ", " (get-msg-list err))]
    (format-code-frame ln col input preamble message)))

