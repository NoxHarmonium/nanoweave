(ns ^{:doc "The nanoweave transform parser.", :author "Sean Dawson"}
    nanoweave.parser.definitions
  (:use [blancas.kern.core]
        [blancas.kern.expr]
        [blancas.kern.lexer.basic]
        [nanoweave.parser.ast]))

; -- Grammar --
; pair          ::=  string ':' (json | expr)
; array         ::=  '[' (json (',' json)*)* ']'
; object        ::=  '{' (pair (',' pair)*)* '}'
; json          ::=  string | number | object | array | true | false | null
; expr          ::= prop-access
; prop-access   ::= identifier ('.' identifier)*

; Wrappers to convert basic types into AST types
(def wrapped-string-lit (>>= string-lit (fn [v] (return (->StringLit v)))))
(def wrapped-float-lit (>>= float-lit (fn [v] (return (->FloatLit v)))))
(def wrapped-bool-lit (>>= bool-lit (fn [v] (return (->BoolLit v)))))
(def wrapped-nil-lit (>>= nil-lit (fn [_] (return (->NilLit)))))

(def concat-op
  "Parses one of the relational operators."
  (bind [op (token "++")]
        (return ({"++" ->ConcatOp} op))))

; Custom base parsers
(defn dot-sep [p] (sep-by dot (lexeme p)))

(declare expr)

(def obj-ref
  "Parses the rule:  prop-access := identifier ('.' identifier)*"
  (>>= (dot-sep identifier) (fn [keys] (return (->ExprPropAccess keys)))))

(def pair
  "Parses the rule:  pair := String ':' expr"
  (bind [f string-lit _ colon v expr] (return [f v])))

(def array
  "Parses the rule:  array := '[' (expr (',' expr)*)* ']'"
  (brackets (comma-sep (fwd expr))))

(def object
  "Parses the rule:  object := '{' (pair (',' pair)*)* '}'"
  (braces (bind [members (comma-sep pair)]
                (return (apply hash-map (reduce concat [] members))))))

(def jvalue
  "Parses a JSON value."
  (<|> wrapped-string-lit
       wrapped-float-lit
       wrapped-bool-lit
       wrapped-nil-lit
       array
       object
       obj-ref
       (parens (fwd expr))))

(def expr (chainl1 jvalue concat-op))