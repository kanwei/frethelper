(ns app.music-theory)

(def notes ["C" "Db" "D" "Eb" "E" "F" "Gb" "G" "Ab" "A" "Bb" "B"])

(def chord-formulas
  {"Major" [0 4 7]
   "Minor" [0 3 7]
   "Major 7" [0 4 7 11]
   "Minor 7" [0 3 7 10]
   "Dominant 7" [0 4 7 10]
   "Minor 6" [0 3 7 9]
   "Major 9" [0 4 7 11 14]
   "Minor 9" [0 3 7 10 14]
   "6th" [0 4 7 9]
   "9th" [0 4 7 10 14]
   "11th" [0 4 7 10 14 17]
   "13th" [0 4 7 10 14 17 21]
   "Augmented" [0 4 8]
   "Augmented 7" [0 4 8 10]
   "Diminished" [0 3 6 9]
   "Diminished 7" [0 3 6 9]
   "Half Diminished" [0 3 6 10]
   "Suspended 2" [0 2 7]
   "Suspended 4" [0 5 7]
   "Add 9" [0 4 7 14]
   "Minor Major 7" [0 3 7 11]})

(def interval-colors
  {0 "#dc2626" ; Root - red
   1 "#f97316" ; Minor 2nd - orange
   2 "#f97316" ; Major 2nd/9th - orange
   3 "#eab308" ; Minor 3rd - yellow
   4 "#eab308" ; Major 3rd - yellow
   5 "#22c55e" ; Perfect 4th/11th - green
   6 "#14b8a6" ; Diminished 5th - teal
   7 "#0ea5e9" ; Perfect 5th - sky blue
   8 "#3b82f6" ; Augmented 5th - blue
   9 "#6366f1" ; Major 6th/13th - indigo
   10 "#8b5cf6" ; Minor 7th - purple
   11 "#8b5cf6"}) ; Major 7th - purple

(defn note-index [note]
  (.indexOf notes note))

(defn normalize-interval [interval]
  (mod interval 12))

(defn get-chord-notes [root chord-type]
  (when-let [formula (get chord-formulas chord-type)]
    (let [root-idx (note-index root)]
      (when (>= root-idx 0)
        (map #(nth notes (mod (+ root-idx %) 12)) formula)))))

(defn get-note-interval [root note]
  (let [root-idx (note-index root)
        note-idx (note-index note)]
    (when (and (>= root-idx 0) (>= note-idx 0))
      (mod (- note-idx root-idx) 12))))

(defn get-interval-color [root note]
  (if-let [interval (get-note-interval root note)]
    (get interval-colors interval "#94a3b8")
    "#94a3b8"))

(defn get-interval-name [interval]
  (case interval
    0 "Root"
    1 "b2"
    2 "2nd/9th"
    3 "b3"
    4 "3rd"
    5 "4th/11th"
    6 "b5"
    7 "5th"
    8 "#5"
    9 "6th/13th"
    10 "b7"
    11 "7th"
    ""))

(defn identify-chord [selected-notes]
  (when (>= (count selected-notes) 3)
    (let [sorted-notes (vec (sort-by note-index selected-notes))]
      (for [root sorted-notes
            [chord-name formula] chord-formulas
            :let [chord-notes (set (get-chord-notes root chord-name))]
            :when (= chord-notes (set selected-notes))]
        {:root root :type chord-name}))))
