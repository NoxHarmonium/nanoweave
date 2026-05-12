(ns nanoweave.demo.core
  (:require [nanoweave.demo.editor :refer [make-editor set-content]]
            [nanoweave.transformers.string-transformer :refer [transform-strings]])
  (:require-macros [nanoweave.demo.examples :refer [load-examples]]))

(def examples (load-examples))

;; State

(defonce state
  (atom {:input (:input (first examples))
         :transform (:transform (first examples))}))

(defonce editors (atom {}))

;; Transform runner

(defn- run-transform []
  (let [{:keys [input transform]} @state
        result (transform-strings input transform)
        output-view (:output @editors)]
    (if (:ok result)
      (set-content output-view
                   (js/JSON.stringify (clj->js (:value result)) nil 2))
      (set-content output-view
                   (str "Error: " (:message (:error result)))))))

;; Debounce

(defonce ^:private debounce-timer (atom nil))

(defn- debounced-run []
  (when @debounce-timer (js/clearTimeout @debounce-timer))
  (reset! debounce-timer (js/setTimeout run-transform 400)))

;; Example selector

(defn- on-example-change [select-el]
  (let [idx (js/parseInt (.-value select-el))
        example (get examples idx)]
    (swap! state assoc
           :input (:input example)
           :transform (:transform example))
    (set-content (:input @editors) (:input example))
    (set-content (:transform @editors) (:transform example))
    (run-transform)))

;; Init

(defn init []
  (let [input-el (.getElementById js/document "editor-input")
        transform-el (.getElementById js/document "editor-transform")
        output-el (.getElementById js/document "editor-output")
        select-el (.getElementById js/document "example-select")]

    ;; Populate dropdown
    (doseq [[i ex] (map-indexed vector examples)]
      (let [opt (.createElement js/document "option")]
        (set! (.-value opt) i)
        (set! (.-textContent opt) (:label ex))
        (.appendChild select-el opt)))

    ;; Build editors
    (reset! editors
            {:input
             (make-editor input-el :json
                          (fn [v]
                            (swap! state assoc :input v)
                            (debounced-run)))
             :transform
             (make-editor transform-el :nanoweave
                          (fn [v]
                            (swap! state assoc :transform v)
                            (debounced-run)))
             :output
             (make-editor output-el :json nil)})

    ;; Load first example
    (set-content (:input @editors) (:input (first examples)))
    (set-content (:transform @editors) (:transform (first examples)))
    (.addEventListener select-el "change" #(on-example-change select-el))
    (run-transform)))
