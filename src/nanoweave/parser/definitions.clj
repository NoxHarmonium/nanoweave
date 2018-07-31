(ns ^{:doc "The nanoweave transform parser.", :author "Sean Dawson"}
nanoweave.parser.definitions
  (:use [blancas.kern.core]
        [blancas.kern.expr]
        [blancas.kern.lexer.java-style]
        [nanoweave.parser.custom-lexing]
        [nanoweave.ast.base]
        [nanoweave.ast.literals]
        [nanoweave.ast.lambda]
        [nanoweave.ast.scope]
        [nanoweave.ast.unary]
        [nanoweave.ast.binary-arithmetic]
        [nanoweave.ast.binary-functions]
        [nanoweave.ast.binary-logic]
        [nanoweave.ast.binary-other]))

(declare expr)

(def pair
  "Parses the rule:  pair := String ':' expr"
  (bind [f string-lit _ colon v expr] (return [f v])))
(def array
  "Parses the rule:  array := '[' (expr (',' expr)*)* ']'"
  (brackets (bind [members (comma-sep (fwd expr))]
                  (return (->ArrayLit members)))))
(def object
  "Parses the rule:  object := '{' (pair (',' pair)*)* '}'"
  (braces (bind [members (comma-sep pair)]
                (return (apply hash-map (reduce concat [] members))))))

(def wrapped-identifier (>>= identifier (fn [v] (return (->IdentiferLit v)))))
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
(def filter-op
  "Filter sequence operator"
  (<?> (bind [op (token "filter")]
             (return ({"filter" ->FilterOp} op)))
       "filter operator"))
(def reduce-op
  "Reduce sequence operator"
  (<?> (bind [op (token "reduce")]
             (return ({"reduce" ->ReduceOp} op)))
       "reduce operator"))
(def fun-ops (<|> map-op filter-op reduce-op))


(def argument-list
  "A list of arguments for a lambda"
  (parens (bind [args (comma-sep identifier)]
                (return args))))
(def lambda-body
  (<|> (parens (fwd expr)) object))
(def lambda
  "A self contained function"
  (bind [args argument-list _ (token "->") body lambda-body]
        (return (->Lambda args body))))

(def no-args-lambda
  "A self contained function with no params definition"
  (bind [_ (token "#") body lambda-body]
        (return (->NoArgsLambda body))))
(def digit-string-lit (lexeme (<+> (many1 digit))))
(def no-args-lambda-param
  "A parameter in a function that has no params definition.
  Just resolves to an identifier for now but might be extended later."
  (bind [prefix (token "%") val digit-string-lit]
        (return (->IdentiferLit (str prefix val)))))


(def variable-binding
  (bind [target identifier
         _ (token "=")
         body (fwd expr)]
        (return (partial ->Binding target body))))

(def binding-list
  (bind [bindings (comma-sep variable-binding)]
        (return (reduce comp bindings))))

(def with-scope
  (bind [_ (token "with")
         bindings binding-list
         _ (token ":")
         body (fwd expr)]
        (return (->Expression (bindings body)))))

(def interpolated-string-expression
  (<?> (bind [_ (token* "#{")
              body (fwd expr)
              _ (token* "}")]
             (return (->Expression body))) "interpolated string expression"))


(def interpolated-string
  "Parses string literals delimited by double quotes."
  (lexeme (between (sym* \")
           (<?> (sym* \") "end interpolated string")
           (many (<|>
                   interpolated-string-expression
                   (<+> (many (java-char [\" \#]))))))))


(def wrapped-interpolated-string (>>= interpolated-string (fn [v] (return (->InterpolatedString v)))))

(def fun-call
  (bind [fun wrapped-identifier
         args (parens (comma-sep (fwd expr)))]
        (return (->FunCall fun args))))

(def nweave
  "Parses a nanoweave structure."
  (<|> with-scope
       (<?> wrapped-interpolated-string "interpolated string")
       (<?> wrapped-float-lit "number")
       (<?> wrapped-bool-lit "bool")
       (<?> wrapped-nil-lit "null")
       (<?> array "array")
       (<?> object "object")
       (<:> fun-call)
       (<?> (<|> wrapped-identifier no-args-lambda-param) "identifer")
       (<:> no-args-lambda)
       (<:> lambda)
       (parens (fwd expr))))

; See: http://www.difranco.net/compsci/C_Operator_Precedence_Table.htm
; Concat group needs to be higher than add group because
; it shares the '+' token
(def member-selection-group (chainl1 nweave dot-op))
(def unary-group (prefix1 member-selection-group wrapped-uni-op))
(def fun-group (chainl1 unary-group fun-ops))
(def concat-group (chainl1 fun-group concat-op))
(def mul-group (chainl1 concat-group wrapped-mul-op))
(def add-group (chainl1 mul-group wrapped-add-op))
(def rel-group (chainl1 add-group wrapped-rel-op))
(def eq-group (chainl1 rel-group wrapped-eq-op))
(def and-group (chainl1 eq-group wrapped-and-op))
(def xor-group (chainl1 and-group wrapped-xor-op))
(def expr (chainl1 xor-group wrapped-or-op))

