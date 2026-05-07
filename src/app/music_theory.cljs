(ns app.music-theory)

(def notes ["C" "Db" "D" "Eb" "E" "F" "Gb" "G" "Ab" "A" "Bb" "B"])

(def circle-of-fourths
  ["C" "F" "Bb" "Eb" "Ab" "Db" "Gb" "B" "E" "A" "D" "G"])

;; Display name of the relative minor root for each major key.
;; Uses sharp-side or flat-side spelling that matches conventional key signature.
(def relative-minor-display
  {"C"  "A"
   "F"  "D"
   "Bb" "G"
   "Eb" "C"
   "Ab" "F"
   "Db" "Bb"
   "Gb" "Eb"
   "B"  "G#"
   "E"  "C#"
   "A"  "F#"
   "D"  "B"
   "G"  "E"})

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

(def diatonic-major-chords
  [{:degree "I"    :interval 0  :quality "Major"      :suffix ""}
   {:degree "ii"   :interval 2  :quality "Minor"      :suffix "m"}
   {:degree "iii"  :interval 4  :quality "Minor"      :suffix "m"}
   {:degree "IV"   :interval 5  :quality "Major"      :suffix ""}
   {:degree "V7"   :interval 7  :quality "Dominant 7" :suffix "7"}
   {:degree "vi"   :interval 9  :quality "Minor"      :suffix "m"}
   {:degree "vii°" :interval 11 :quality "Diminished" :suffix "°"}])

(defn get-diatonic-chords [root]
  (let [root-idx (note-index root)]
    (when (>= root-idx 0)
      (for [{:keys [degree interval quality suffix]} diatonic-major-chords
            :let [chord-root (nth notes (mod (+ root-idx interval) 12))]]
        {:degree degree
         :root chord-root
         :quality quality
         :name (str chord-root suffix)}))))

;; Close-voiced triad search.
;; String indexing here is low-E-first: 0=low E, 1=A, 2=D, 3=G, 4=B, 5=high E.
(def open-string-class [4 9 2 7 11 4])
(def open-string-pitch [0 5 10 15 19 24])

;; Bass strings — chord voicings always start on low E (0) or A (1).
(def bass-strings [0 1])

(defn- triad-intervals [quality]
  (case quality
    "Major"      [0 4 7]   ; R, M3, P5
    "Minor"      [0 3 7]   ; R, m3, P5
    "Dominant 7" [0 4 10]  ; R, M3, b7 (3-note shell omitting 5th)
    "Diminished" [0 3 6]   ; R, m3, b5
    nil))

(defn- inversions [chord-classes]
  (let [[a b c] chord-classes]
    [[a b c] [b c a] [c a b]]))

(defn- lowest-fret [string-idx note-class]
  (mod (- note-class (nth open-string-class string-idx)) 12))

(defn- pitch-at [string-idx fret]
  (+ (nth open-string-pitch string-idx) fret))

(defn- ascending-fret [string-idx note-class min-pitch]
  (loop [f (lowest-fret string-idx note-class)]
    (if (> (pitch-at string-idx f) min-pitch)
      f
      (recur (+ f 12)))))

(defn- next-chord-tone-fret [string-idx chord-classes prev-pitch]
  ;; Among all chord-class candidates on this string, pick the one with the lowest fret
  ;; whose pitch is >= prev-pitch. Returns nil if none fit within fret 12.
  (let [candidates (for [nc chord-classes
                         :let [base (lowest-fret string-idx nc)
                               f (loop [f0 base]
                                   (if (>= (pitch-at string-idx f0) prev-pitch)
                                     f0
                                     (recur (+ f0 12))))]
                         :when (<= f 12)]
                     {:fret f :pitch (pitch-at string-idx f)})]
    (when (seq candidates)
      (apply min-key :fret candidates))))

(def chord-connection-window 3)

;; Variations per chord quality. :label is the key persisted in settings,
;; :suffix is appended to the chord root for display, :intervals are semitones.
(def chord-variations
  {"Major"      [{:label ""     :suffix ""        :intervals [0 4 7]}
                 {:label "6"    :suffix "6"       :intervals [0 4 7 9]}
                 {:label "maj7" :suffix "maj7"    :intervals [0 4 7 11]}
                 {:label "7"    :suffix "7"       :intervals [0 4 7 10]}]
   "Minor"      [{:label ""     :suffix "m"       :intervals [0 3 7]}
                 {:label "m6"   :suffix "m6"      :intervals [0 3 7 9]}
                 {:label "m7"   :suffix "m7"      :intervals [0 3 7 10]}
                 {:label "mM7"  :suffix "m(maj7)" :intervals [0 3 7 11]}]
   "Dominant 7" [{:label ""     :suffix ""        :intervals [0 4 7]}
                 {:label "7"    :suffix "7"       :intervals [0 4 7 10]}]
   "Diminished" [{:label ""     :suffix "°"       :intervals [0 3 6]}
                 {:label "°7"   :suffix "°7"      :intervals [0 3 6 9]}
                 {:label "m7b5" :suffix "ø7"      :intervals [0 3 6 10]}]})

(defn variations-for [quality]
  (get chord-variations quality []))

(defn intervals-for [quality variation-label]
  (let [vars (variations-for quality)
        match (or (some #(when (= (:label %) variation-label) %) vars)
                  (first vars))]
    (:intervals match)))

(defn chord-tone-positions
  "All fretboard positions (fret 0-12, every string, low-E-first index) where a
  chord tone occurs given the supplied intervals from the chord root."
  [chord-root intervals]
  (when (and chord-root (seq intervals))
    (let [root-idx (note-index chord-root)
          classes (set (mapv #(mod (+ root-idx %) 12) intervals))]
      (vec (for [string-idx (range 6)
                 fret (range 13)
                 :let [note-class (mod (+ (nth open-string-class string-idx) fret) 12)]
                 :when (contains? classes note-class)]
             {:string string-idx :fret fret})))))

(defn chord-connections [positions]
  ;; Pairs of chord-tone positions whose fret distance is within the window, either:
  ;; - on adjacent strings, or
  ;; - on the same string (consecutive chord tones).
  (let [by-string (group-by :string positions)
        adjacent (for [s (range 5)
                       p1 (get by-string s [])
                       p2 (get by-string (inc s) [])
                       :when (<= (Math/abs (- (:fret p1) (:fret p2))) chord-connection-window)]
                   [p1 p2])
        same-string (for [s (range 6)
                          :let [ps (sort-by :fret (get by-string s []))]
                          [p1 p2] (partition 2 1 ps)
                          :when (<= (- (:fret p2) (:fret p1)) chord-connection-window)]
                      [p1 p2])]
    (vec (concat adjacent same-string))))

(defn- hex->rgb [hex]
  (let [h (subs hex 1)]
    [(js/parseInt (subs h 0 2) 16)
     (js/parseInt (subs h 2 4) 16)
     (js/parseInt (subs h 4 6) 16)]))

(defn- ->hex2 [n]
  (let [s (.toString (max 0 (min 255 (Math/round n))) 16)]
    (if (= 1 (count s)) (str "0" s) s)))

(defn lighten [hex amount]
  ;; amount in [-1, 1]. Positive lightens toward white, negative darkens toward black.
  (let [[r g b] (hex->rgb hex)
        adjust (fn [c]
                 (if (pos? amount)
                   (+ c (* (- 255 c) amount))
                   (* c (+ 1 amount))))]
    (str "#" (->hex2 (adjust r)) (->hex2 (adjust g)) (->hex2 (adjust b)))))

;; Diatonic chord colors keyed by scale degree (rainbow per scale step).
(def diatonic-colors
  {"I"    "#dc2626"
   "ii"   "#f97316"
   "iii"  "#eab308"
   "IV"   "#22c55e"
   "V7"   "#0ea5e9"
   "vi"   "#6366f1"
   "vii°" "#8b5cf6"})

(defn identify-chord [selected-notes]
  (when (>= (count selected-notes) 3)
    (let [sorted-notes (vec (sort-by note-index selected-notes))]
      (for [root sorted-notes
            [chord-name formula] chord-formulas
            :let [chord-notes (set (get-chord-notes root chord-name))]
            :when (= chord-notes (set selected-notes))]
        {:root root :type chord-name}))))
