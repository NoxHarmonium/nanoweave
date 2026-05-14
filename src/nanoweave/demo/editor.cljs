(ns nanoweave.demo.editor
  (:require ["@codemirror/view" :refer [EditorView lineNumbers]]
            ["@codemirror/state" :refer [EditorState]]
            ["@codemirror/lang-json" :refer [json]]
            ["@codemirror/theme-one-dark" :refer [oneDark]]
            [nanoweave.demo.nanoweave-mode :refer [nanoweave-language]]))

(def ^:private read-only-extension
  (.of (.-editable EditorView) false))

(defn- on-update-ext [handler]
  (.of (.-updateListener EditorView) handler))

(defn make-editor
  "Creates a CodeMirror EditorView mounted in `parent-el`.
   `language`  - one of :json, :nanoweave, or :plain
   `on-change` - called with new doc string on every change (nil for read-only)
   Returns the EditorView."
  [parent-el language on-change]
  (let [lang-ext (case language
                   :json (json)
                   :nanoweave nanoweave-language
                   nil)
        change-ext (when on-change
                     (on-update-ext
                      (fn [^js update]
                        (when (.-docChanged update)
                          (on-change (.. update -state -doc (toString)))))))
        extensions (cond-> [(lineNumbers) oneDark]
                     lang-ext (conj lang-ext)
                     on-change (conj change-ext)
                     (not on-change) (conj read-only-extension))
        state (.create EditorState #js {:doc ""
                                        :extensions (into-array extensions)})]
    (EditorView. #js {:state state :parent parent-el})))

(defn set-content
  "Replaces the entire content of `view` with `text`."
  [view text]
  (.dispatch view
             (.update (.-state view)
                      #js {:changes #js {:from 0
                                         :to (.. view -state -doc -length)
                                         :insert text}})))
