(ns ^{:doc    "The jweave transform parser."
      :author "Sean Dawson"}
jweave.parser.definitions
  (:use [blancas.kern.core]
        [blancas.kern.lexer.basic]
        [jweave.parser.ast])
  (:require jweave.parser.ast)
  (:import [jweave.parser.ast StringLit FloatLit BoolLit NilLit ExprPropAccess]))

; -- Grammar --
; pair    ::=  string ':' (json | expr)
; array   ::=  '[' (json (',' json)*)* ']'
; object  ::=  '{' (pair (',' pair)*)* '}'
; json    ::=  string | number | object | array | true | false | null
; expr    ::= string ('.' string)*

(def wrapped-string-lit (>>= string-lit (fn [v] (return (->StringLit v)))))
(def wrapped-float-lit (>>= float-lit (fn [v] (return (->FloatLit v)))))
(def wrapped-bool-lit (>>= bool-lit (fn [v] (return (->BoolLit v)))))
(def wrapped-nil-lit (>>= nil-lit (fn [_] (return (->NilLit)))))

(declare jvalue)

;(defn lol
;  "Parses p one or more times while parsing sep in between;
;   collects the results of p in a vector."
;  [sep p] (>>= p (fn [x] (>>= (many (>> sep p)) (fn [y] (return (reduce conj [x] y)))))))

(defn dot-sep [p] (sep-by dot (lexeme p)))
;(def obj-ref (>>= (dot-sep identifier) (fn [keys] (return (reduce comp (map (fn [key] (partial read-value key)) keys))))))
(def obj-ref (>>= (dot-sep identifier) (fn [keys] (return (->ExprPropAccess keys)))))

(def expr
  "Parses the rule:  expr := Identifier ('.' Identifier)*"
  obj-ref)

;(reduce comp (map (partial read-value) (dot-sep identifier))))

(def pair
  "Parses the rule:  pair := String ':' jvalue"
  (bind [f string-lit _ colon v jvalue]
        (return [f v])))

(def array
  "Parses the rule:  array := '[' (jvalue (',' jvalue)*)* ']'"
  (brackets (comma-sep (fwd jvalue))))

(def object
  "Parses the rule:  object := '{' (pair (',' pair)*)* '}'"
  (braces
    (bind [members (comma-sep pair)]
          (return (apply hash-map (reduce concat [] members))))))

(def jvalue
  "Parses a JSON value."
  (<|> wrapped-string-lit wrapped-float-lit wrapped-bool-lit wrapped-nil-lit array object expr))