(ns app.core
  (:require [uix.core :as uix :refer [defui $]]
            [uix.dom]
            [app.music-theory :as theory]
            [app.fretboard :as fb]))

(defui note-button [{:keys [note string-idx fret-idx selected-key selected-chord show-all
                            chord-notes clicked-notes on-note-click]}]
  (let [is-chord-note (contains? chord-notes note)
        is-clicked (contains? clicked-notes [string-idx fret-idx])
        show-note (or show-all is-chord-note)
        interval (theory/get-note-interval selected-key note)
        is-root (= interval 0)
        color (if (and selected-key show-note)
                (theory/get-interval-color selected-key note)
                "#94a3b8")]
    (when show-note
      ($ :div {:class (str "note-dot"
                           (when is-clicked " selected")
                           (when is-root " root"))
               :style {:background-color color}
               :on-click #(on-note-click [string-idx fret-idx] note)}
         note))))

(defui fretboard-component [{:keys [selected-key selected-chord show-all
                                    clicked-notes on-note-click]}]
  (let [fretboard-notes (fb/generate-fretboard-notes)
        chord-notes (if (and selected-key selected-chord)
                      (set (theory/get-chord-notes selected-key selected-chord))
                      #{})]
    ($ :div {:class "fretboard"}
       ($ :div {:class "fret-numbers"}
          (for [fret (range 13)]
            ($ :div {:key fret :class "fret-number"}
               (when (> fret 0) fret))))

       (for [string-idx (range 6)]
         (let [string-notes (nth fretboard-notes string-idx)]
           ($ :div {:key string-idx :class "string"}
              ($ :div {:class "string-label"}
                 (nth fb/standard-tuning string-idx))
              ($ :div {:class (str "string-line string-line-" string-idx)})
              ($ :div {:class "frets"}
                 (for [fret-idx (range 13)]
                   ($ :div {:key fret-idx :class "fret"}
                      ($ note-button {:note (nth string-notes fret-idx)
                                      :string-idx string-idx
                                      :fret-idx fret-idx
                                      :selected-key selected-key
                                      :selected-chord selected-chord
                                      :show-all show-all
                                      :chord-notes chord-notes
                                      :clicked-notes clicked-notes
                                      :on-note-click on-note-click}))))))))))

(defui chord-identifier [{:keys [clicked-notes-map on-clear]}]
  (let [selected-notes (vec (vals clicked-notes-map))
        identified-chords (when (>= (count selected-notes) 3)
                            (theory/identify-chord selected-notes))]
    ($ :div {:class "chord-identifier"}
       ($ :h3 {:class "identifier-title"} "🎵 Chord Identifier")

       (when (pos? (count selected-notes))
         ($ :div
            ($ :div {:class "selected-notes"}
               (for [note selected-notes]
                 ($ :div {:key note :class "selected-note"} note)))

            ($ :div {:class "identified-chords"}
               (if (seq identified-chords)
                 (for [{:keys [root type]} identified-chords]
                   ($ :div {:key (str root "-" type) :class "chord-match"}
                      root " " type))
                 (when (>= (count selected-notes) 3)
                   ($ :div {:class "no-match"} "No matching chord found"))))

            ($ :button {:class "clear-button"
                        :on-click on-clear}
               "Clear Selection"))))))

(defui legend [{:keys [selected-key]}]
  ($ :div {:class "legend"}
     ($ :div {:class "legend-title"} "Rainbow Chord Colors:")
     ($ :div {:class "legend-items"}
        (for [[interval label] [[0 "Root (1)"]
                                [2 "2nd/9th"]
                                [4 "3rd"]
                                [5 "4th/11th"]
                                [7 "5th"]
                                [9 "6th/13th"]
                                [10 "7th"]]]
          ($ :div {:key interval :class "legend-item"}
             ($ :div {:class "legend-dot"
                      :style {:background-color (get theory/interval-colors interval)}})
             ($ :span label))))))

(defui app []
  (let [[selected-key set-selected-key] (uix/use-state "C")
        [selected-chord set-selected-chord] (uix/use-state "6th")
        [show-all set-show-all] (uix/use-state false)
        [clicked-notes set-clicked-notes] (uix/use-state {})

        handle-note-click (fn [position note]
                            (set-clicked-notes
                             (fn [notes]
                               (if (contains? notes position)
                                 (dissoc notes position)
                                 (assoc notes position note)))))

        handle-clear (fn [] (set-clicked-notes {}))]

    ($ :div {:class "app-container"}
       ($ :div {:class "header"}
          ($ :h1 {:class "title"}
             "🎸 FretHelper - Interactive Guitar Chord Visualizer")
          ($ :p {:class "subtitle"}
             "Select a key and chord type to see it visualized on the fretboard with rainbow colors!"))

       ($ :div {:class "controls"}
          ($ :div {:class "control-group"}
             ($ :label {:class "control-label"} "Select Key:")
             ($ :div {:class "button-group"}
                (for [note theory/notes]
                  ($ :button {:key note
                              :class (str "key-button"
                                          (when (= note selected-key) " selected"))
                              :on-click #(set-selected-key note)}
                     note))))

          ($ :div {:class "control-group"}
             ($ :label {:class "control-label"} "Select Chord Type:")
             ($ :div {:class "button-group"}
                (for [chord-type (keys theory/chord-formulas)]
                  ($ :button {:key chord-type
                              :class (str "chord-button"
                                          (when (= chord-type selected-chord) " selected"))
                              :on-click #(set-selected-chord chord-type)}
                     chord-type))))

          ($ :button {:class "show-all-button"
                      :on-click #(set-show-all (not show-all))}
             (if show-all "Show Chord Notes Only" "Show All Notes")))

       ($ legend {:selected-key selected-key})

       ($ :div {:class "fretboard-container"}
          ($ :h2 {:class "fretboard-title"}
             (str selected-key selected-chord))
          ($ fretboard-component {:selected-key selected-key
                                  :selected-chord selected-chord
                                  :show-all show-all
                                  :clicked-notes clicked-notes
                                  :on-note-click handle-note-click}))

       ($ chord-identifier {:clicked-notes-map clicked-notes
                            :on-clear handle-clear}))))

(defonce root
  (uix.dom/create-root (js/document.getElementById "root")))

(defn render []
  (uix.dom/render-root
   ($ uix/strict-mode
      ($ app))
   root))

(defn ^:export init []
  (render))