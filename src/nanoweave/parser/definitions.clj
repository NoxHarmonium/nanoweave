(ns ^{:doc "The nanoweave parser definitions.", :author "Sean Dawson"}
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

; Forward declarations

(declare expr)

; JSON Elements

(def pair
  "Parses the rule:  pair := String ':' expr"
  (<?> (bind [f string-lit
              _ colon
              v expr] (return [f v]))
       "pair"))
(def array
  "Parses the rule:  array := '[' (expr (',' expr)*)* ']'"
  (<?> (brackets (bind [members (comma-sep (fwd expr))]
                       (return (->ArrayLit members))))
       "array"))
(def object
  "Parses the rule:  object := '{' (pair (',' pair)*)* '}'"
  (<?> (braces (bind [members (comma-sep pair)]
                     (return (apply hash-map (reduce concat [] members)))))
       "object"))

; Wrapped Primatives

(def wrapped-identifier
  (<?> (bind [v identifier]
             (return (->IdentiferLit v)))
       "identifier"))
(def wrapped-float-lit
  "Wraps a float-lit parser so it returns an AST record rather than a float."
  (<?> (bind [v float-lit]
             (return (->FloatLit (double v))))
       "float"))
(def wrapped-bool-lit
  "Wraps a bool-lit parser so it returns an AST record rather than a bool."
  (<?> (bind [v bool-lit] (return (->BoolLit v)))
       "boolean"))
(def wrapped-nil-lit
  "Wraps an nil-lit parser so it returns an AST record rather than a null."
  (<?> (bind [_ nil-lit] (return (->NilLit)))
       "null"))

; Unary Operators

(def wrapped-uni-op
  "Unary operators: not or negative."
  (<?> (bind [op (one-of "!-")]
             (return ({\! ->NotOp \- ->NegOp} op)))
       "unary operator (!,-)"))

; Arithmetic Binary Operators

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

; Logical Binary Operators

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

; Functional Binary Operators

(def map-op
  "Map sequence operator"
  (<?> (bind [_ (token "map")]
             (return ->MapOp))
       "map operator"))
(def filter-op
  "Filter sequence operator"
  (<?> (bind [_ (token "filter")]
             (return ->FilterOp))
       "filter operator"))
(def reduce-op
  "Reduce sequence operator"
  (<?> (bind [_ (token "reduce")]
             (return ->ReduceOp))
       "reduce operator"))

(def fun-ops
  "Matches any of the functional binary operators (they have the same precidence)"
  (<|> map-op filter-op reduce-op))

; Lambdas

(def argument-list
  "A list of arguments for a lambda"
  (<?> (parens (bind [args (comma-sep identifier)]
                     (return args)))
       "lambda argument list"))
(def lambda-body
  "The body of a lambda that is executed when the lambda is called"
  (<?> (<|> (parens (fwd expr)) object)
       "lambda body"))
(def lambda
  "A self contained function that binds an expression to arguments"
  (<?> (bind [args argument-list
              _ (token "->")
              body lambda-body]
             (return (->Lambda args body)))
       "lambda"))
(def no-args-lambda
  "A self contained function with no params definition"
  (<?> (bind [_ (token "#")
              body lambda-body]
             (return (->NoArgsLambda body)))
       "lambda"))
(def digit-string-lit
  "Parses a continous string of digits to a trimmed string"
  (lexeme (<+> (many1 digit))))
(def no-args-lambda-param
  "A parameter in a function that has no params definition.
  Just resolves to an identifier for now but might be extended later."
  (<?> (bind [prefix (token "%")
              val digit-string-lit]
             (return (->IdentiferLit (str prefix val))))
       "lambda parameter"))
(def function-call
  "Calls a lambda with specified params"
  (<?> (bind [args (parens (comma-sep (fwd expr)))]
             (return #(->FunCall %1 args)))
       "function call"))

; Other Binary Operators

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

; Scopes

(def variable-binding
  "Parses a binding of an expression result to a variable"
  (<?> (bind [target identifier
              _ (token "=")
              body (fwd expr)]
             (return (partial ->Binding target body)))
       "variable binding"))
(def binding-list
  "Parses multiple variable bindings separated by commas to a sequence"
  (<?> (bind [bindings (comma-sep variable-binding)]
             (return (reduce comp bindings)))
       "binding list"))
(def let-scope
  "Creates a new scope that has variables that are bound in the binding list"
  (<?> (bind [_ (token "let")
              bindings binding-list
              _ (token ":")
              body (fwd expr)]
             (return (->Expression (bindings body))))
       "let statement"))
(def indexing
  "Indexes a map or a sequence by a key"
  (<?> (bind [key (brackets (fwd expr))]
             (return #(->Indexing %1 key)))
       "indexing"))

; Interpolated String

(def interpolated-string-expression
  "Parses an expression embedded within a string"
  (<?> (bind [_ (token* "#{")
              body (fwd expr)
              _ (token* "}")]
             (return (->Expression body))) "interpolated string expression"))
(def interpolated-string
  "Parses string literals and embedded expressions delimited by double quotes"
  (lexeme (between (sym* \")
                   (<?> (sym* \") "end string")
                   (many (<|>
                           interpolated-string-expression
                           (<+> (many (string-char [\" \#]))))))))
(def wrapped-interpolated-string
  "Wraps an interpolated-string parser so it returns an AST record rather than an array of strings and expressions."
  (<?> (bind [v interpolated-string]
             (return (->InterpolatedString v)))
       "string"))

; Root Definition

(def nweave
  "Parses a nanoweave structure."
  (<|> let-scope
       wrapped-interpolated-string
       wrapped-float-lit
       wrapped-bool-lit
       wrapped-nil-lit
       array
       object
       (<|> wrapped-identifier no-args-lambda-param)
       (<:> no-args-lambda)
       (<:> lambda)
       (parens (fwd expr))))

; See: http://www.difranco.net/compsci/C_Operator_Precedence_Table.htm
; Concat group needs to be higher than add group because
; it shares the '+' token
(def member-selection-group (chainl1 nweave dot-op))
(def apply-group (postfix1 member-selection-group (<|> function-call indexing)))
(def unary-group (prefix1 apply-group wrapped-uni-op))
(def fun-group (chainl1 unary-group fun-ops))
(def concat-group (chainl1 fun-group concat-op))
(def mul-group (chainl1 concat-group wrapped-mul-op))
(def add-group (chainl1 mul-group wrapped-add-op))
(def rel-group (chainl1 add-group wrapped-rel-op))
(def eq-group (chainl1 rel-group wrapped-eq-op))
(def and-group (chainl1 eq-group wrapped-and-op))
(def xor-group (chainl1 and-group wrapped-xor-op))
(def expr (chainl1 xor-group wrapped-or-op))

