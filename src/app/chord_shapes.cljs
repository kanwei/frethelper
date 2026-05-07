(ns app.chord-shapes
  "Comprehensive chord shape database for guitar chord visualization")

;; Chord shape database with all forms from Mel Bay book
;; Each chord shape contains:
;; - :frets - fret positions for each string (nil = don't play, 0 = open)
;; - :root - string index where root note is located
;; - :barres - fret numbers that require barré technique
;; - :name - display name of the form

(def chord-shapes
  {;; Major Chords (Mel Bay Forms I, II, III)
   ;; Each form is shown at its lowest position; :base names the chord at that position.
   "major" {"I"   {:frets [1 3 3 2 1 1]   :root 0 :barres [1] :base "F"  :name "Form I (E-shape)"}
            "II"  {:frets [nil 4 3 1 2 nil] :root 1 :barres [] :base "Db" :name "Form II (C-shape)"}
            "III" {:frets [4 3 1 1 1 nil]   :root 0 :barres [1] :base "Ab" :name "Form III (G-shape)"}}

   ;; Minor Chords (Forms 1m, 2m, 3m)
   "minor" {"1m" {:frets [nil 1 1 1 nil 3] :root 1 :barres [] :name "Form 1m"}
            "2m" {:frets [nil nil 1 2 3 4] :root 2 :barres [] :name "Form 2m"}
            "3m" {:frets [1 1 nil 1 2 3] :root 0 :barres [1] :name "Form 3m"}}

   ;; Dominant 7th Chords (Mel Bay Forms I7, III7, V7, VII7).
   ;; All forms play low E + D + G + B; A and high E are deadened.
   "dominant7" {"I7"   {:frets [3 nil 2 3 1 nil] :root 4 :barres []  :base "C"  :name "Form I7"}
                "III7" {:frets [2 nil 1 1 1 nil] :root 3 :barres [1] :base "Ab" :name "Form III7"}
                "V7"   {:frets [1 nil 1 2 1 nil] :root 0 :barres [1] :base "F"  :name "Form V7"}
                "VII7" {:frets [3 nil 1 3 2 nil] :root 2 :barres []  :base "Eb" :name "Form VII7"}}

   ;; Minor 7th Chords (Forms Im7, IIIm7, Vm7, VIIm7)
   "minor7" {"Im7" {:frets [nil nil 1 nil 2 3] :root 2 :barres [] :name "Form Im7"}
             "IIIm7" {:frets [nil nil nil 1 1 1 2] :root 3 :barres [1] :name "Form IIIm7"}
             "Vm7" {:frets [nil nil 1 2 3 2] :root 2 :barres [] :name "Form Vm7"}
             "VIIm7" {:frets [nil nil nil 1 nil 2 3] :root 3 :barres [] :name "Form VIIm7"}}

   ;; Major 7th Chords (Forms IIIMa7, VMa7, VIIMa7, 2Ma7)
   "major7" {"IIIMa7" {:frets [nil nil nil 1 1 1 2] :root 3 :barres [1] :name "Form IIIMa7"}
             "VMa7" {:frets [nil nil 1 2 3 4] :root 2 :barres [] :name "Form VMa7"}
             "VIIMa7" {:frets [nil nil nil 1 nil 2 3] :root 3 :barres [] :name "Form VIIMa7"}
             "2Ma7" {:frets [nil 1 1 2 3 nil] :root 1 :barres [1] :name "Form 2Ma7"}}

   ;; 7♭5 Chords (Forms I7♭5, III7♭5, V7♭5, VII7♭5)
   "7b5" {"I7b5" {:frets [nil nil 1 2 nil 3] :root 2 :barres [] :name "Form I7♭5"}
          "III7b5" {:frets [nil nil nil 1 1 nil 2] :root 3 :barres [1] :name "Form III7♭5"}
          "V7b5" {:frets [nil nil 1 2 nil 3] :root 2 :barres [] :name "Form V7♭5"}
          "VII7b5" {:frets [nil nil nil 1 nil nil 2] :root 3 :barres [] :name "Form VII7♭5"}}

   ;; 7#5 Chords (Forms I7#5, III7#5, V7#5, VII7#5)
   "7#5" {"I7#5" {:frets [nil nil 1 2 4 3] :root 2 :barres [] :name "Form I7#5"}
          "III7#5" {:frets [nil nil nil 1 1 4 2] :root 3 :barres [1] :name "Form III7#5"}
          "V7#5" {:frets [nil nil 1 2 4 3] :root 2 :barres [] :name "Form V7#5"}
          "VII7#5" {:frets [nil nil nil 1 nil 4 2] :root 3 :barres [] :name "Form VII7#5"}}

   ;; Major 6th Chords (Forms I6, III6, V6, VI6)
   "6th" {"I6" {:frets [nil nil 1 2 nil 3] :root 2 :barres [] :name "Form I6"}
          "III6" {:frets [nil nil nil 2 3 3 1] :root 3 :barres [3] :name "Form III6"}
          "V6" {:frets [nil nil 1 nil 2 3] :root 2 :barres [] :name "Form V6"}
          "VI6" {:frets [nil nil nil 1 1 nil 3] :root 3 :barres [1] :name "Form VI6"}}

   ;; Minor 6th Chords (Forms Im6, IIIm6, Vm6, VIm6)
   "minor6" {"Im6" {:frets [nil nil 1 nil 1 2] :root 2 :barres [] :name "Form Im6"}
             "IIIm6" {:frets [nil nil nil 2 3 3 1] :root 3 :barres [3] :name "Form IIIm6"}
             "Vm6" {:frets [nil nil 1 nil 3 2] :root 2 :barres [] :name "Form Vm6"}
             "VIm6" {:frets [nil 1 1 nil 2 nil] :root 1 :barres [1] :name "Form VIm6"}}

   ;; Diminished Chords (Forms I°, III°, V°, VII°)
   "diminished" {"Idim" {:frets [nil nil 1 2 nil 3] :root 2 :barres [] :name "Form I°"}
                 "IIIdim" {:frets [nil nil nil 1 1 2 nil] :root 3 :barres [1] :name "Form III°"}
                 "Vdim" {:frets [nil nil 1 2 3 2] :root 2 :barres [] :name "Form V°"}
                 "VIIdim" {:frets [nil nil nil 1 nil 2 3] :root 3 :barres [] :name "Form VII°"}}

   ;; Augmented Chords
   "augmented" {"aug" {:frets [1 nil 1 2 3 4] :root 0 :barres [] :name "Augmented"}}

   ;; 9th Chords (Forms I9, III9, V9, VII9)
   "9th" {"I9" {:frets [nil nil 1 2 3 4] :root 2 :barres [] :name "Form I9"}
          "III9" {:frets [nil nil nil 1 1 2 3] :root 3 :barres [1] :name "Form III9"}
          "V9" {:frets [nil nil 1 2 3 nil] :root 2 :barres [] :name "Form V9"}
          "VII9" {:frets [nil nil nil 1 nil 2 3] :root 3 :barres [] :name "Form VII9"}}

   ;; Minor 9th Chords (Forms Im9, Vm9, VIIm9)
   "minor9" {"Im9" {:frets [nil nil 1 nil 3 2] :root 2 :barres [] :name "Form Im9"}
             "Vm9" {:frets [nil 1 1 1 nil 3] :root 1 :barres [1] :name "Form Vm9"}
             "VIIm9" {:frets [nil nil nil 1 3 4] :root 3 :barres [] :name "Form VIIm9"}}

   ;; 9#5 Chords (Forms I9#5, III9#5, V9#5, VII9#5)
   "9#5" {"I9#5" {:frets [nil nil 1 nil 3 4] :root 2 :barres [] :name "Form I9#5"}
          "III9#5" {:frets [nil nil nil 1 1 nil 2] :root 3 :barres [1] :name "Form III9#5"}
          "V9#5" {:frets [nil 1 nil 1 2 3] :root 1 :barres [] :name "Form V9#5"}
          "VII9#5" {:frets [nil nil 1 2 nil 3] :root 2 :barres [] :name "Form VII9#5"}}

   ;; 9♭5 Chords (Forms I9♭5, III9♭5, V9♭5, VII9♭5)
   "9b5" {"I9b5" {:frets [nil nil 1 nil 2 3] :root 2 :barres [] :name "Form I9♭5"}
          "III9b5" {:frets [nil nil nil 1 nil 2 3] :root 3 :barres [] :name "Form III9♭5"}
          "V9b5" {:frets [nil 1 nil 1 2 nil] :root 1 :barres [] :name "Form V9♭5"}
          "VII9b5" {:frets [nil nil 1 nil 2 3] :root 2 :barres [] :name "Form VII9♭5"}}

   ;; Major 9th Chords (Forms IMa9, IIIMa9, VMa9, VIIMa9)
   "major9" {"IMa9" {:frets [nil nil 1 2 3 4] :root 2 :barres [] :name "Form IMa9"}
             "IIIMa9" {:frets [nil nil nil 1 1 2 3] :root 3 :barres [1] :name "Form IIIMa9"}
             "VMa9" {:frets [nil nil 1 2 3 4] :root 2 :barres [] :name "Form VMa9"}
             "VIIMa9" {:frets [nil nil nil 1 2 3 3] :root 3 :barres [3] :name "Form VIIMa9"}}

   ;; 11th Chords (Forms I11, III11, V11, VII11)
   "11th" {"I11" {:frets [nil nil 1 2 3 3] :root 2 :barres [3] :name "Form I11"}
           "III11" {:frets [nil 1 nil 2 3 4] :root 1 :barres [] :name "Form III11"}
           "V11" {:frets [nil 1 1 nil 3 4] :root 1 :barres [1] :name "Form V11"}
           "VII11" {:frets [nil nil nil 1 3 4] :root 3 :barres [] :name "Form VII11"}}

   ;; 13th Chords (Forms 113, 213, 313, 413)
   "13th" {"113" {:frets [nil 1 2 nil 3 4] :root 1 :barres [] :name "Form 113"}
           "213" {:frets [1 1 1 1 2 3] :root 0 :barres [1] :name "Form 213"}
           "313" {:frets [1 1 nil 2 3 4] :root 0 :barres [1] :name "Form 313"}
           "413" {:frets [nil nil 1 2 3 3] :root 2 :barres [3] :name "Form 413"}}

   ;; 6/9 Chords (Forms 16/9, 26/9, 36/9)
   "6/9" {"16/9" {:frets [nil nil 1 2 nil 3] :root 2 :barres [] :name "Form 16/9"}
          "26/9" {:frets [nil 1 1 nil 3 3] :root 1 :barres [1 3] :name "Form 26/9"}
          "36/9" {:frets [1 nil 1 2 3 4] :root 0 :barres [] :name "Form 36/9"}}})

;; Display names for chord types
(def chord-type-names
  {"major" "Major"
   "minor" "Minor"
   "dominant7" "Dominant 7th"
   "minor7" "Minor 7th"
   "major7" "Major 7th"
   "7b5" "7♭5"
   "7#5" "7#5"
   "6th" "6th"
   "minor6" "Minor 6th"
   "diminished" "Diminished"
   "augmented" "Augmented"
   "9th" "9th"
   "minor9" "Minor 9th"
   "9#5" "9#5"
   "9b5" "9♭5"
   "major9" "Major 9th"
   "11th" "11th"
   "13th" "13th"
   "6/9" "6/9"})

;; Notes for transposition (match music_theory.cljs)
(def notes-for-transposition ["C" "Db" "D" "Eb" "E" "F" "Gb" "G" "Ab" "A" "Bb" "B"])

(defn get-available-forms [chord-type]
  "Get all available forms for a given chord type"
  (keys (get chord-shapes chord-type {})))

(defn get-chord-shape [chord-type form]
  "Get the chord shape data for a specific chord type and form"
  (get-in chord-shapes [chord-type form]))

(defn transpose-chord-shape [chord-shape semitones]
  "Shift every fret and barre in a shape up by the given number of semitones."
  (when chord-shape
    (-> chord-shape
        (update :frets
                (fn [frets]
                  (mapv (fn [fret]
                          (cond
                            (nil? fret) nil
                            (= fret 0) semitones
                            :else (+ fret semitones)))
                        frets)))
        (update :barres (fn [bs] (mapv #(+ % semitones) (or bs [])))))))

(defn get-transposed-chord [key chord-type form]
  "Get a chord shape transposed to the specified key. If the form declares a
  :base note, the offset is computed relative to that; otherwise the key index
  is used as the absolute offset (legacy behavior)."
  (when-let [base-shape (get-chord-shape chord-type form)]
    (let [target-idx (.indexOf notes-for-transposition key)]
      (when (>= target-idx 0)
        (let [base-idx (when-let [b (:base base-shape)]
                         (.indexOf notes-for-transposition b))
              semitones (if (and base-idx (>= base-idx 0))
                          (mod (- target-idx base-idx) 12)
                          target-idx)]
          (transpose-chord-shape base-shape semitones))))))
