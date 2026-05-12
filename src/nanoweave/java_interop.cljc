(ns nanoweave.java-interop)

#?(:cljs
   (do
     (defn call-static-method [_target _key _marshalled-args]
       (js/console.warn "call-static-method not supported in ClojureScript:" key)
       nil)

     (defn call-instance-method [_target _key _marshalled-args]
       (js/console.warn "call-instance-method not supported in ClojureScript:" key)
       nil)

     (defn get-static-field [_target _key]
       (js/console.warn "get-static-field not supported in ClojureScript:" key)
       nil)

     (defn get-instance-field [_target _key]
       (js/console.warn "get-instance-field not supported in ClojureScript:" key)
       nil)

     (defn members-matching-name [_instance _key] nil)

     (defn matches-reflect-type? [_instance _key _reflect-type _is-static] nil)

     (defn marshal-arg [arg]
       (cond
         (or (seq? arg) (vector? arg)) (to-array arg)
         :else arg))

     (defn marshal-return [val]
       (cond
         (number? val) (double val)
         :else val))

     (defn wrap-java-fn [_target _key _is-static]
       (fn [_input & _args]
         (js/console.warn "Java method call not supported in ClojureScript:" key)
         nil))

     (defn call-java-constructor [class _args]
       (js/console.warn "call-java-constructor not supported in ClojureScript:" class)
       nil)

     (defn get-java-field [_target _key _is-static]
       (js/console.warn "get-java-field not supported in ClojureScript:" key)
       nil)

     (defn byte-value [_target]
       (js/console.warn "byte-value not supported in ClojureScript")
       nil)

     (defn double-value [_target] _target)

     (defn float-value [_target]
       (js/console.warn "float-value not supported in ClojureScript")
       nil)

     (defn int-value [_target] _target)

     (defn long-value [_target]
       (js/console.warn "long-value not supported in ClojureScript")
       nil)

     (defn short-value [_target]
       (js/console.warn "short-value not supported in ClojureScript")
       nil)

     (defn java-cast [clazz _target]
       (js/console.warn "java-cast not supported in ClojureScript:" clazz)
       nil)))
