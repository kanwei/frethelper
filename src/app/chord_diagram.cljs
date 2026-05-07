(ns app.chord-diagram
  (:require [uix.core :as uix :refer [defui $]]
            [app.chord-shapes :as shapes]))

(defui chord-diagram [{:keys [chord-shape chord-name form-name]}]
  (when chord-shape
    (let [{:keys [frets barres root]} chord-shape
          num-frets 5
          num-strings 6
          string-names ["E" "A" "D" "G" "B" "E"]]

      ($ :div {:class "chord-diagram"}
         ;; Chord name and form
         ($ :div {:class "chord-header"}
            ($ :h3 {:class "chord-name"} chord-name)
            ($ :p {:class "form-name"} form-name))

         ;; SVG diagram
         ($ :svg {:width 200 :height 240 :view-box "0 0 200 240" :class "chord-svg"}
            ;; Fret lines
            (for [i (range (inc num-frets))]
              ($ :line {:key (str "fret-" i)
                        :x1 20
                        :y1 (+ 20 (* i 40))
                        :x2 180
                        :y2 (+ 20 (* i 40))
                        :stroke "#333"
                        :stroke-width (if (= i 0) "3" "1")}))

            ;; String lines  
            (for [i (range num-strings)]
              ($ :line {:key (str "string-" i)
                        :x1 (+ 20 (* i 32))
                        :y1 20
                        :x2 (+ 20 (* i 32))
                        :y2 220
                        :stroke "#333"
                        :stroke-width "1"}))

            ;; Barré indicators
            (for [[idx fret] (map-indexed vector barres)]
              ($ :line {:key (str "barre-" idx)
                        :x1 20
                        :y1 (- (+ 20 (* fret 40)) 20)
                        :x2 180
                        :y2 (- (+ 20 (* fret 40)) 20)
                        :stroke "#e74c3c"
                        :stroke-width "8"
                        :stroke-linecap "round"
                        :opacity "0.7"}))

            ;; Finger positions
            (for [[string-idx fret] (map-indexed vector frets)
                  :when (and fret (> fret 0))]
              (let [x (+ 20 (* string-idx 32))
                    y (- (+ 20 (* fret 40)) 20)
                    is-root (= string-idx root)]
                ($ :g {:key (str "finger-" string-idx)}
                   ($ :circle {:cx x
                               :cy y
                               :r 12
                               :fill (if is-root "#e74c3c" "#3498db")
                               :stroke "#2c3e50"
                               :stroke-width "2"})
                   ($ :text {:x x
                             :y (+ y 4)
                             :text-anchor "middle"
                             :font-size "12"
                             :fill "white"
                             :font-weight "bold"}
                      (str fret)))))

            ;; Muted strings (X) and open strings (O)
            (for [[string-idx fret] (map-indexed vector frets)]
              (let [x (+ 20 (* string-idx 32))]
                (cond
                  (nil? fret)
                  ($ :text {:key (str "muted-" string-idx)
                            :x x
                            :y 10
                            :text-anchor "middle"
                            :font-size "16"
                            :fill "#e74c3c"
                            :font-weight "bold"}
                     "X")

                  (= fret 0)
                  ($ :circle {:key (str "open-" string-idx)
                              :cx x
                              :cy 5
                              :r 8
                              :fill "none"
                              :stroke "#27ae60"
                              :stroke-width "3"}))))

            ;; Fret numbers
            (for [i (range num-frets)]
              ($ :text {:key (str "fret-num-" i)
                        :x 5
                        :y (+ 40 (* i 40))
                        :text-anchor "middle"
                        :font-size "12"
                        :fill "#666"}
                 (str (inc i))))

            ;; String labels
            (for [[idx string-name] (map-indexed vector string-names)]
              ($ :text {:key (str "string-label-" idx)
                        :x (+ 20 (* idx 32))
                        :y 235
                        :text-anchor "middle"
                        :font-size "10"
                        :fill "#666"}
                 string-name)))))))

(defui chord-shapes-section [{:keys [selected-key selected-chord-type on-chord-type-change]}]
  (let [available-forms (shapes/get-available-forms selected-chord-type)
        chord-type-display (get shapes/chord-type-names selected-chord-type selected-chord-type)]

    ($ :div {:class "chord-shapes-section"}
       ;; Chord type selector with buttons
       ($ :div {:class "chord-controls"}
          ($ :div {:class "control-group"}
             ($ :label {:class "control-label"} "Select Chord Type:")
             ($ :div {:class "chord-type-buttons"}
                (for [[chord-key chord-name] shapes/chord-type-names]
                  ($ :button {:key chord-key
                              :class (str "chord-type-button"
                                          (when (= chord-key selected-chord-type) " selected"))
                              :on-click #(on-chord-type-change chord-key)}
                     chord-name)))))

       ;; Display all chord forms side by side
       ($ :div {:class "chord-forms-grid"}
          (for [form available-forms]
            (let [chord-shape (shapes/get-transposed-chord selected-key selected-chord-type form)
                  chord-name (str selected-key
                                  (if (= "major" selected-chord-type)
                                    ""
                                    chord-type-display))
                  form-name (get-in chord-shape [:name] "")]
              ($ :div {:key form :class "chord-form-item"}
                 (if chord-shape
                   ($ chord-diagram {:chord-shape chord-shape
                                     :chord-name chord-name
                                     :form-name form-name})
                   ($ :div {:class "no-chord"}
                      ($ :p "No chord shape available")))))))

       ;; Info section
       ($ :div {:class "chord-info"}
          ($ :h4 "Chord Diagram Guide:")
          ($ :ul
             ($ :li "All forms are movable to any key")
             ($ :li "Red dots = Root notes")
             ($ :li "Blue dots = Other chord tones")
             ($ :li "Red bars = Barré technique")
             ($ :li "X = Muted string")
             ($ :li "O = Open string"))))))
