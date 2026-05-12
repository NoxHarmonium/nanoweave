(ns ^{:doc "Custom lexers to help with parsing.", :author "Sean Dawson"}
 nanoweave.parsers.custom-lexing
  (:require [blancas.kern.core :refer
             [>>= <+> <?> <|> >> many1 return fail one-of* sym* times satisfy hex-digit oct-digit bind token-]]))

; Duplicates blancas.kern.lexer but I couldn't work out another way
; to create a custom string type because they are all private in Kern.

(def space-ascii 32)

(def esc-oct
  "Parses an octal escape code; the result is the encoded char."
  (>>= (<+> (many1 oct-digit))
       (fn [x]
         (let [n #?(:clj (Integer/parseInt x 8)
                    :cljs (js/parseInt x 8))]
           (if (<= n 255)
             (return (char n))
             (fail "bad octal sequence"))))))

(def esc-char
  "Parses an escape code for a basic char."
  (let [codes (zipmap "btnfr'\"\\/" "\b\t\n\f\r'\"\\/")]
    (>>= (<?> (one-of* "btnfr'\"\\/") "escape character")
         (fn [x] (return (get codes x))))))

(def esc-uni
  "Parses a unicode escape code; the result is the encoded char."
  (>>= (<+> (>> (sym* \u) (times 4 hex-digit)))
       (fn [x] (return #?(:clj (aget (Character/toChars (Integer/parseInt x 16)) 0)
                          :cljs (.fromCodePoint js/String (js/parseInt x 16)))))))

(defn string-char
  "Parses an unquoted Java character literal. Characters in terminators must be escaped."
  [terminators]
  (<?> (<|> (satisfy #(and (not-any? (partial = %) terminators) (not= % \\) (>= #?(:clj (int %) :cljs (.charCodeAt % 0)) space-ascii)))
            (>> (sym* \\)
                (<?> (<|> esc-char esc-oct esc-uni)
                     "escaped code: b, t, n, f, r, ', \\, ooo, hhhh")))
       "character literal"))

(defn regex-char
  "Parses a character for a regex literal. Forward slashes must be escaped."
  []
  (<?> (<|> (bind [_ (token- "\\/")] (return \/))
            (satisfy #(and (not= % \/) (>= #?(:clj (int %) :cljs (.charCodeAt % 0)) space-ascii))))
       "regex literal"))
