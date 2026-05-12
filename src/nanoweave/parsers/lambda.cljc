(ns ^{:doc "Parses operations to define functions and call them."
      :author "Sean Dawson"}
 nanoweave.parsers.lambda
  (:require [blancas.kern.core :refer [bind <|> <+> <?> return fail look-ahead many1 digit]]
            [blancas.kern.lexer.java-style :refer
             [comma-sep identifier parens token sym lexeme]]
            [nanoweave.ast.lambda :refer [->Lambda ->NoArgsLambda ->FunCall ->ArgList]]
            [nanoweave.ast.literals :refer [->IdentiferLit]]
            [nanoweave.parsers.base :refer [<s> pop-span fwd-expr]]
            [nanoweave.parsers.literals :refer [object-lit]]))

; Forward declarations

; (declare-extern replaced by fwd-expr for cross-platform support)

; Lambdas

(def argument-list
  "A list of arguments for a lambda"
  (<?> (parens (bind [args (comma-sep identifier)]
                     (return args)))
       "lambda argument list"))
(def lambda-body
  "The body of a lambda that is executed when the lambda is called"
  (<?> (<|> (parens (fwd-expr)) object-lit)
       "lambda body"))
(def lambda
  "A self contained function that binds an expression to arguments"
  (<s> (<?> (bind [args argument-list
                   _ (token "->")
                   body lambda-body
                   ps pop-span]
                  (return ((ps ->Lambda) args body)))
            "lambda")))
(def no-args-lambda
  "A self contained function with no params definition"
  (<s> (<?> (bind [_ (sym \#)
                   body lambda-body
                   ps pop-span]
                  (return ((ps ->NoArgsLambda) body)))
            "lambda")))
(def digit-string-lit
  "Parses a continuous string of digits to a trimmed string"
  (lexeme (<+> (many1 digit))))
(def no-args-lambda-param
  "A parameter in a function that has no params definition.
  Just resolves to an identifier for now but might be extended later."
  (<s> (<?> (bind [prefix (sym \%)
                   val digit-string-lit
                   ps pop-span]
                  (return ((ps ->IdentiferLit) (str prefix val) false)))
            "lambda parameter")))
(def function-call
  "Calls a lambda/java function with specified params.
   This parser is constructed differently from the others so
   that function calling can be treated as a binary operation
   without having an explicit operator."
  (bind [ahead (look-ahead (sym \())]
        (if ahead
          (return ->FunCall)
          (fail "expected ("))))
(def function-arguments
  "A list of expressions passed to function application"
  (<s> (<?> (bind [arg-exprs (comma-sep (fwd-expr))
                   ps pop-span]
                  (return ((ps ->ArgList) arg-exprs)))
            "function arguments")))
