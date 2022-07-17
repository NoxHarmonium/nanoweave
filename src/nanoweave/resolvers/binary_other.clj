(ns nanoweave.resolvers.binary-other
  (:require [nanoweave.ast.binary-other]
            [nanoweave.resolvers.base :refer
             [handle-bin-op handle-prop-access safe-resolve-value]]
            [nanoweave.utils :refer [dynamically-load-class convert-to-number]]
            [nanoweave.ast.base :refer [Resolvable]])
  (:import [nanoweave.ast.binary_other DotOp ConcatOp OpenRangeOp ClosedRangeOp IsOp AsOp]))

(defn all-sequential? [coll]
  (reduce #(and (sequential? %1) %2) coll))

(extend-protocol Resolvable
  DotOp
  (resolve-value [this input] (handle-prop-access this input))
  ConcatOp
  (resolve-value [this input]
    (let [left (safe-resolve-value (:left this) input)
          right (safe-resolve-value (:right this) input)]
      (if
       (all-sequential? [left right]) ((comp vec concat) left right)
       (str left right))))
  OpenRangeOp
  (resolve-value [this input] (handle-bin-op this input (comp vec range)))
  ClosedRangeOp
  (resolve-value [this input] (handle-bin-op this input (comp vec #(range %1 (inc %2)))))
  IsOp
  (resolve-value [this input]
    (handle-bin-op this input
                   #(case %2
                      :number (number? %1)
                      :string  (string? %1)
                      :boolean  (boolean? %1)
                      :nil   (nil? %1)
                      ; TODO: Should we check for seqable??
                      :array (vector? %1)
                      (if (string? %2) (instance? (dynamically-load-class %2) %1)
                          (throw (AssertionError. (str "Unknown type '" (type %2) "' for type checking. Should either be one of the type literals Number, String, Boolean, Nil or Array or a string referring to a fully qualified Java class")))))))
  AsOp
  (resolve-value [this input]
    (handle-bin-op this input
                   #(case %2
                      :number (convert-to-number %1)
                      :string  (str %1)
                      :boolean  (boolean %1)
                      ; Not sure why you would cast to null but I guess it is valid type
                      :nil   nil
                      :array (vec %1)
                      (if (string? %2)
                        (let [referenced-class (dynamically-load-class %2)]
                          ; Special case to get numbers into the right type for JVM methods
                          (if (instance? java.lang.Number %1) (case %2
                                                                ; TODO: Wrap these to fix reflection linting issues
                                                                "java.lang.Byte" (byte-value %1)
                                                                "java.lang.Double" (double-value %1)
                                                                "java.lang.Float" (float-value %1)
                                                                "java.lang.Integer" (int-value %1)
                                                                "java.lang.Long" (long-value %1)
                                                                "java.lang.Short" (short-value %1)
                                                                (throw (AssertionError. (str "java.lang.Number subclass of '" %2 "' is not supported. Currenly supported are Byte, Double, Float, Int, Long and Short (when fully qualified)"))))
                              (java-cast referenced-class %1)))
                        (throw (AssertionError. (str "Unknown type '" (type %2) "' for type coercion. Should either be one of the type literals Number, String, Boolean, Nil or Array or a string referring to a fully qualified Java class"))))))))