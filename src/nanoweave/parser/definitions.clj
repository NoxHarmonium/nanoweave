(ns ^{:doc "The nanoweave parser definitions.", :author "Sean Dawson"}
 nanoweave.parser.definitions
  (:require [blancas.kern.core :refer :all]
            [blancas.kern.expr :refer :all]
            [blancas.kern.lexer.java-style :refer :all]
            [nanoweave.parser.custom-lexing :refer :all]
            [nanoweave.ast.base :refer :all]
            [nanoweave.ast.literals :refer :all]
            [nanoweave.ast.lambda :refer :all]
            [nanoweave.ast.scope :refer :all]
            [nanoweave.ast.unary :refer :all]
            [nanoweave.ast.binary-arithmetic :refer :all]
            [nanoweave.ast.binary-functions :refer :all]
            [nanoweave.ast.binary-logic :refer :all]
            [nanoweave.ast.binary-other :refer :all]))

; Forward declarations

(declare expr)

; JSON Elements

(def pair
  "Parses the rule:  pair := String ':' expr"
  (<?> (bind [f string-lit
              _ colon
              v (fwd expr)] (return [f v]))
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

; Wrapped Primitives

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
  "Matches any of the functional binary operators (they have the same precedence)"
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
  (<?> (bind [_ (sym \#)
              body lambda-body]
             (return (->NoArgsLambda body)))
       "lambda"))
(def digit-string-lit
  "Parses a continuous string of digits to a trimmed string"
  (lexeme (<+> (many1 digit))))
(def no-args-lambda-param
  "A parameter in a function that has no params definition.
  Just resolves to an identifier for now but might be extended later."
  (<?> (bind [prefix (sym \%)
              val digit-string-lit]
             (return (->IdentiferLit (str prefix val))))
       "lambda parameter"))
(def function-call
  "Calls a lambda/java function with specified params.
   This parser is constructed differently from the others so
   that function calling can be treated as a binary operation
   without having an explicit operator."
  (bind [ahead (look-ahead (sym \())]
        (if ahead
          (return ->FunCall)
          (fail "expected ("))))

; Other Binary Operators


(def dot-op
  "Access operator: extract value from object."
  (<?> (bind [_ dot]
             (return ->DotOp))
       "property accessor (.)"))
(def concat-op
  "Parses one of the relational operators."
  (<?> (bind [_ (token "++")]
             (return ->ConcatOp))
       "concat operator (++)"))
(def open-range-op
  "Parses an open range expression."
  (<?> (bind [_ (token "until")]
             (return ->OpenRangeOp))
       "open range expression (until)"))
(def closed-range-op
  "Parses an closed range expression."
  (<?> (bind [_ (token "to")]
             (return ->ClosedRangeOp))
       "closed range expression (to)"))

; Scopes

(declare binding-target)
(def literal-match
  "pareses an expression that pattern matches against a literal variable"
  (<?> (bind [match (fwd expr)]
             (return (->LiteralMatchOp match)))
       "literal pattern match"))
(def variable-match
  "parses an expression that pattern matches against a single variable"
  (<?> (bind [match identifier]
             (return (->VariableMatchOp match)))
       "variable pattern match"))
(def list-pattern-match
  "parses an expression that pattern matches against a list"
  (<?> (bind [_ (token "^")
              match (brackets (comma-sep (fwd binding-target)))]
             (return (->ListPatternMatchOp match)))
       "list pattern match"))
(def map-pattern-match
  "parses an expression that pattern matches against a map structure"
  (<?> (bind [_ (token "^")
              match (braces (comma-sep identifier))]
             (return (->MapPatternMatchOp match)))
       "map pattern match"))

(def binding-target
  "parses the target of a variable binding"
  (<?> (<|>
        (<:> list-pattern-match)
        (<:> map-pattern-match)
        variable-match
        literal-match)
       "variable binding target"))

(def variable-binding
  "Parses a binding of an expression result to a target"
  (<?> (bind [target binding-target
              _ (sym \=)
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
              _ colon
              body (fwd expr)]
             (return (->Expression (bindings body))))
       "let statement"))
(def when-clause
  "An expression and an associated body that will be evaluated if the expression evaluates truthy"
  (<?> (bind [condition (fwd expr)
              _ colon
              body (fwd expr)]
             (return (->WhenClause condition body)))
       "when clause"))
(def when-scope
  "A flow control construct that will take a branch if an expression evaluates truthy"
  (<?> (bind [_ (token "when")
              clauses (comma-sep when-clause)]
             (return (->When clauses)))
       "when statement"))
(def else
  "Always evaluates to true, used in a when clause to always execute a clause"
  (<?> (bind [_ (token "else")]
             (return (->BoolLit true)))
       "else"))
(def match-clause
  "A pattern match expression and an associated body that will be evaluated if the match succeeds"
  (<?> (bind [match binding-target
              _ colon
              body (fwd expr)]
             (return (->MatchClause match body)))
       "match clause"))
(def match-scope
  "A match construct will take a branch if a pattern matches and passes in the matched variables"
  (<?> (bind [_ (token "match")
              clauses (parens (comma-sep match-clause))]
             (return (partial ->Match clauses)))
       "match statement"))

(def indexing
  "Indexes a map or a sequence by a key.
  This parser is constructed differently from the others so
   that indexing can be treated as a binary operation
   without having an explicit operator."
  (bind [ahead (look-ahead (sym \[))]
        (if ahead
          (return ->Indexing)
          (fail "expected ["))))

(def import-statement
  "Imports a JVM class into scope"
  (<?> (bind [_ (token "import")
              class string-lit]
             (return (->ImportOp class)))
       "import"))
(def function-arguments
  "A list of expressions passed to function application"
  (<?> (bind [arguments (comma-sep (fwd expr))]
             (return (->ArgList arguments)))
       "function arguments"))


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
  (<|>
   let-scope
   when-scope
   import-statement
   wrapped-interpolated-string
   wrapped-float-lit
   wrapped-bool-lit
   else
   wrapped-nil-lit
   array
   object
   (<|> wrapped-identifier no-args-lambda-param)
   (<:> no-args-lambda)
   (<:> lambda)
   (<:> (parens (fwd expr)))
   (parens function-arguments)))

; See: http://www.difranco.net/compsci/C_Operator_Precedence_Table.htm
; Concat group needs to be higher than add group because
; it shares the '+' token

(def fun-group (chainl1 nweave fun-ops))
(def member-access-group (chainl1 fun-group
                                  (<|> dot-op (<:> function-call) (<:> indexing))))
(def concat-group (chainl1 member-access-group concat-op))
(def unary-group (prefix1 concat-group wrapped-uni-op))
(def mul-group (chainl1 unary-group wrapped-mul-op))
(def add-group (chainl1 mul-group wrapped-add-op))
(def rel-group (chainl1 add-group wrapped-rel-op))
(def range-group (chainl1 rel-group
                          (<|> open-range-op closed-range-op)))
(def eq-group (chainl1 range-group wrapped-eq-op))
(def and-group (chainl1 eq-group wrapped-and-op))
(def xor-group (chainl1 and-group wrapped-xor-op))
(def or-group (chainl1 xor-group wrapped-or-op))
(def expr (postfix1 or-group match-scope))
