(ns ^{:doc "The nanoweave transform parser.", :author "Sean Dawson"}
nanoweave.parser.definitions
  (:use [blancas.kern.core]
        [blancas.kern.expr]
        [blancas.kern.lexer.basic]
        [nanoweave.ast.base]
        [nanoweave.ast.literals]
        [nanoweave.ast.lambda]
        [nanoweave.ast.unary]
        [nanoweave.ast.binary-arithmetic]
        [nanoweave.ast.binary-functions]
        [nanoweave.ast.binary-logic]
        [nanoweave.ast.binary-other]))

; -- Grammar --
; pair          ::=  string ':' (json | expr)
; array         ::=  '[' (json (',' json)*)* ']'
; object        ::=  '{' (pair (',' pair)*)* '}'
; json          ::=  string | number | object | array | true | false | null
; expr          ::= prop-access
; prop-access   ::= identifier ('.' identifier)*

; Wrappers to convert basic types into AST types

(declare expr)

(def wrapped-identifier (>>= identifier (fn [v] (return (->IdentiferLit v)))))
(def wrapped-string-lit (>>= string-lit (fn [v] (return (->StringLit v)))))
(def wrapped-float-lit (>>= float-lit (fn [v] (return (->FloatLit (double v))))))
(def wrapped-bool-lit (>>= bool-lit (fn [v] (return (->BoolLit v)))))
(def wrapped-nil-lit (>>= nil-lit (fn [_] (return (->NilLit)))))
(def dot-op
  "Access operator: extract value from object."
  (<?> (bind [op (token ".")]
             (return ({"." ->DotOp} op)))
       "property accessor (.)"))
(def concat-op
  "Parses one of the relational operators."
  (<?> (bind [op (token "++")]
             (return ({"++" ->ConcatOp} op)))
       "concat operator (++)"))
(def wrapped-uni-op
  "Unary operators: not or negative."
  (<?> (bind [op (one-of "!-")]
             (return ({\! ->NotOp \- ->NegOp} op)))
       "unary operator (!,-)"))
(def wrapped-mul-op
  "Multiplicative operator: multiplication, division, or modulo."
  (<?> (bind [op (one-of "*/%")]
             (return ({\* ->MultOp \/ ->DivOp \% ->ModOp} op)))
       "multiplication operator (*,/,%)"))
(def wrapped-add-op
  "Additive operator: addition or subtraction."
  (<?> (bind [op (one-of "+-")]
             (return ({\+ ->AddOp \- ->SubOp} op)))
       "addition operator (+,-)"))
(def wrapped-rel-op
  "Relational operator: greater than, less than"
  (<?> (bind [op (token ">=" "<=" ">" "<")]
             (return ({">=" ->GrThanEqOp "<=" ->LessThanEqOp ">" ->GrThanOp "<" ->LessThanOp} op)))
       "relational operator (>=,<=,>,<)"))
(def wrapped-eq-op
  "Equality operator: equal or not-equal"
  (<?> (bind [op (token "==" "!=")]
             (return ({"==" ->EqOp "!=" ->NotEqOp} op)))
       "equality operator (==,!=)"))
(def wrapped-and-op
  "Logical AND operator"
  (<?> (bind [op (token "and")]
             (return ({"and" ->AndOp} op)))
       "and operator"))
(def wrapped-or-op
  "Logical OR operator"
  (<?> (bind [op (token "or")]
             (return ({"or" ->OrOp} op)))
       "or operator"))
(def wrapped-xor-op
  "Logical XOR operator"
  (<?> (bind [op (token "xor")]
             (return ({"xor" ->XorOp} op)))
       "xor operator"))
(def map-op
  "Map sequence operator"
  (<?> (bind [op (token "map")]
             (return ({"map" ->MapOp} op)))
       "map operator"))


(def argument-list
  "A list of arguments for a lambda"
  (parens (bind [args (comma-sep identifier)]
                (return args))))
(def lambda
  "A self contained function"
  (bind [args argument-list _ (token "->") body (fwd expr)]
        (return (->Lambda args body))))
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

(def nweave
  "Parses a nanoweave structure."
  (<|> (<?> wrapped-string-lit "string")
       (<?> wrapped-float-lit "number")
       (<?> wrapped-bool-lit "bool")
       (<?> wrapped-nil-lit "null")
       (<?> array "array")
       (<?> object "object")
       (<?> wrapped-identifier "identifer")
       (<:> lambda)
       (parens (fwd expr))))

; See: http://www.difranco.net/compsci/C_Operator_Precedence_Table.htm
; Concat group needs to be higher than add group because
; it shares the '+' token
(def member-selection-group (chainl1 nweave dot-op))
(def unary-group (prefix1 member-selection-group wrapped-uni-op))
(def concat-group (chainl1 unary-group concat-op))
(def mul-group (chainl1 concat-group wrapped-mul-op))
(def add-group (chainl1 mul-group wrapped-add-op))
(def rel-group (chainl1 add-group wrapped-rel-op))
(def eq-group (chainl1 rel-group wrapped-eq-op))
(def and-group (chainl1 eq-group wrapped-and-op))
(def xor-group (chainl1 and-group wrapped-xor-op))
(def fun-group (chainl1 xor-group map-op))
(def expr (chainl1 fun-group wrapped-or-op))

