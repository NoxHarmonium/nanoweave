(ns
 ^{:doc "Error handling functions.", :author "Sean Dawson"}
 nanoweave.transformers.errors
  (:require [blancas.kern.core :refer :all]
            [blancas.kern.i18n :refer :all]
            [clojure.string :refer [join]]))

; Copied from https://github.com/blancas/kern/blob/master/src/main/clojure/blancas/kern/core.clj
; because the methods were not made public

;; Error types.
(def- err-system 0)                                         ;; Used in satisfy for specific unexpected input.
(def- err-unexpect 1)                                       ;; Used on any unexpected input to show a message.
(def- err-expect 2)                                         ;; Used to show a message of what's expected.
(def- err-message 3)                                        ;; Used for any kind of message from client code.

(defn- make-err-system
  "Makes a message of type err-system."
  [pos text] (->PError pos (list (->PMessage err-system text))))

(defn- make-err-unexpect
  "Makes a message of type err-unexpect."
  [pos text] (->PError pos (list (->PMessage err-unexpect text))))

(defn- make-err-expect
  "Makes a message of type err-expect."
  [pos text] (->PError pos (list (->PMessage err-expect text))))

(defn- make-err-message
  "Makes a message of type err-message."
  [pos text] (->PError pos (list (->PMessage err-message text))))

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
     (let [lst (filter #(= (:type %) err-system) ms)]
       (reduce #(conj %1 (get-msg %2)) [] lst))
     (let [lst (filter #(= (:type %) err-unexpect) ms)]
       (reduce #(conj %1 (get-msg %2)) [] lst))
     (let [lst (filter #(= (:type %) err-expect) ms)]
       (if (empty? lst) lst (list (get-msg-expect lst))))
     (let [lst (filter #(= (:type %) err-message) ms)]
       (reduce #(conj %1 (get-msg %2)) [] lst)))))

(defn- get-msg-str
  "Gets the text of error messages separated by \\n."
  [err]
  (let [eol (System/getProperty "line.separator")]
    (join eol (get-msg-list err))))

(defn format-error
  "Formats error messages in a PState record."
  [s]
  (let [err (:error s)
        pos (:pos err)
        src (let [l (:src pos)] (if (empty? l) "" (str l " ")))
        ln (:line pos)
        col (:col pos)]
    (let [eol (System/getProperty "line.separator")]
      (str (format (i18n :err-pos) src ln col)
           eol
           (join eol (get-msg-list err))))))