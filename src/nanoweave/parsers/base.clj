(ns ^{:doc "Parses the basic structure of a transform definition.", :author "Sean Dawson"}
 nanoweave.parsers.base
  (:require [blancas.kern.core :refer [<|> bind put-state return]]
            [nanoweave.ast.base :refer [->AstPos ->AstSpan]]
            [nanoweave.utils :refer [declare-extern]]
            [schema.core :refer [validate]])
  (:import [nanoweave.ast.base AstSpan]))

; Forward declarations

(declare-extern nanoweave.parsers.expr/expr)

;; Utility

(defn <s>
  "Pushes the current position to the user state so it can be used to calculate the AST span"
  [p]
  (fn [current-state]
    (let [current-pos (:pos current-state)
          current-user-state (:user current-state)
          prev-stack (:prev-position current-user-state)
          orig-stack-len (count prev-stack)
          pushed-stack (conj prev-stack current-pos)
          final-state ((put-state {:prev-position pushed-stack}) current-state)
          output (p final-state)
          after-stack-length (count (:prev-position (:user current-state)))]
      (assert (= orig-stack-len after-stack-length) "Mismatched calls to <s> and pop-span. There should be one call to pop-span for each call to <s>")
      output)))

(defn pop-span
  "Wraps an AST constructor so that the AST span is automatically applied as an argument after parsing"
  [current-state]
  (let [curr-pos (:pos current-state)
        current-user-state (:user current-state)
        prev-stack (:prev-position current-user-state)
        prev-pos (peek prev-stack)
        span (->AstSpan
              (->AstPos (:line prev-pos) (:col prev-pos) (:src prev-pos))
              (->AstPos (:line curr-pos) (dec (:col curr-pos)) (:src curr-pos)))
        popped-stack (pop prev-stack)
        new-state ((put-state {:prev-position popped-stack}) current-state)
        constructor-fn (fn [rec & args]
                         (fn [& more-args]
                           (apply rec span (concat args more-args))))]
    ((return constructor-fn) new-state)))

; TODO Move this

(defn merge-span
  "Takes two AST objects and creates a new AstSpan object that spans the two objects.
   Usually used to create span for binary operators so the span covers both sides of the operator."
  [a b]
  (let [span-a (:span a) span-b (:span b)]
    (validate AstSpan span-a)
    (validate AstSpan span-b)
    (->AstSpan (:start span-a) (:end span-b))))

(defn chainl1*
  "Specialisation of Kern's chainl1 function that ensures that binary operator AST
   have spans the cover both sides of the operator.
   
   Parses p; as long as there is a binary operator op, reads the op and
   another instance of p, then applies the operator on both values.
   The operator associates to the left."
  [p op]
  (letfn [(rest [a] (<|> (bind [f op b p] (rest (f (merge-span a b) a b)))
                         (return a)))]
    (bind [a p] (rest a))))
