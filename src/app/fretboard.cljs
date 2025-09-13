(ns app.fretboard
  (:require [app.music-theory :as theory]))

(def standard-tuning ["E" "B" "G" "D" "A" "E"])

(defn get-note-at-fret [open-note fret]
  (let [open-idx (theory/note-index open-note)]
    (nth theory/notes (mod (+ open-idx fret) 12))))

(defn generate-fretboard-notes []
  (vec (for [string-idx (range 6)]
         (let [open-note (nth standard-tuning string-idx)]
           (vec (for [fret (range 13)]
                  (get-note-at-fret open-note fret)))))))
