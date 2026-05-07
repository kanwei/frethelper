(ns app.core
  (:require [uix.core :as uix :refer [defui $]]
            [uix.dom]
            [clojure.string :as str]
            ["@mantine/core" :refer [MantineProvider Modal Tabs SegmentedControl
                                     Button Switch Chip Group Stack Title Text
                                     Paper ActionIcon]]
            ["@mantine/core" :as mantine]
            [app.music-theory :as theory]
            [app.fretboard :as fb]
            [app.chord-diagram :as chord-diagram]
            [app.chord-shapes :as shapes]
            [app.settings :as settings]))

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
       ($ :div {:class "fret-numbers fret-numbers-top"}
          (for [fret (range fb/num-frets)]
            ($ :div {:key fret :class "fret-number"}
               (when (> fret 0) fret))))

       (for [string-idx (range 6)]
         (let [string-notes (nth fretboard-notes string-idx)]
           ($ :div {:key string-idx :class "string"}
              ($ :div {:class "string-label"}
                 (nth fb/standard-tuning string-idx))
              ($ :div {:class (str "string-line string-line-" string-idx)})
              ($ :div {:class "frets"}
                 (for [fret-idx (range fb/num-frets)]
                   ($ :div {:key fret-idx :class (str "fret fret-" fret-idx)}
                      ($ note-button {:note (nth string-notes fret-idx)
                                      :string-idx string-idx
                                      :fret-idx fret-idx
                                      :selected-key selected-key
                                      :selected-chord selected-chord
                                      :show-all show-all
                                      :chord-notes chord-notes
                                      :clicked-notes clicked-notes
                                      :on-note-click on-note-click})))))))

       ($ :div {:class "fret-numbers fret-numbers-bottom"}
          (for [fret (range fb/num-frets)]
            ($ :div {:key fret :class "fret-number"}
               (when (> fret 0) fret)))))))

(defn render-chord-name [root quality minor-notation]
  (case quality
    "Minor"      (if (= minor-notation "-")
                   ($ :span root ($ :sup {:class "minor-sup"} "-"))
                   ($ :span root "m"))
    "Dominant 7" ($ :span root "7")
    "Diminished" ($ :span root "°")
    ($ :span root)))

(defn- to-fb-position [{:keys [string fret]}]
  (let [fb-idx (- 5 string)
        open-note (nth fb/standard-tuning fb-idx)]
    {:string-idx fb-idx
     :fret fret
     :note (fb/get-note-at-fret open-note fret)}))

(defn caged-positions [chord-root quality]
  ;; Returns {:positions [...] :connections [[a b] ...]} in fretboard string
  ;; convention (0 = high E, 5 = low E). Positions are every chord tone on the
  ;; fretboard; connections are pairs on adjacent strings within 3 frets.
  (let [tones (theory/chord-tone-positions chord-root quality)
        edges (theory/chord-connections tones)]
    {:positions (mapv to-fb-position tones)
     :connections (mapv (fn [[a b]] [(to-fb-position a) (to-fb-position b)]) edges)}))

(defn- pct [n d] (str (* 100 (/ n d)) "%"))

(defui caged-overlay [{:keys [selected-key active-degrees]}]
  (let [chords (theory/get-diatonic-chords selected-key)
        active-chords (filterv #(contains? active-degrees (:degree %)) chords)
        chord-data (mapv (fn [{:keys [degree root quality]}]
                           (let [data (caged-positions root quality)
                                 color (get theory/diatonic-colors degree "#666")]
                             (assoc data :degree degree :color color)))
                         active-chords)]
    ($ :div {:class "caged-overlay"}
       ;; SVG layer: just the connection lines (stretches with the fretboard).
       ($ :svg {:class "caged-lines"
                :view-box (str "0 0 " fb/num-frets " 6")
                :preserve-aspect-ratio "none"}
          (for [{:keys [degree color connections]} chord-data]
            ($ :g {:key degree}
               (for [[idx [a b]] (map-indexed vector connections)
                     :let [same-string? (= (:string-idx a) (:string-idx b))
                           ;; Same-string connections are exactly horizontal and would
                           ;; lie along the string line; arc them slightly above the
                           ;; string with a quadratic curve so they're clearly visible.
                           x1 (+ (:fret a) 0.5)
                           y1 (+ (:string-idx a) 0.5)
                           x2 (+ (:fret b) 0.5)
                           y2 (+ (:string-idx b) 0.5)
                           mid-x (/ (+ x1 x2) 2)
                           ctrl-y (- y1 0.35)]]
                 (if same-string?
                   ($ :path {:key idx
                             :d (str "M " x1 "," y1 " Q " mid-x "," ctrl-y " " x2 "," y2)
                             :fill "none"
                             :stroke color
                             :stroke-width 0.05
                             :stroke-linecap "round"})
                   ($ :line {:key idx
                             :x1 x1 :y1 y1 :x2 x2 :y2 y2
                             :stroke color
                             :stroke-width 0.05
                             :stroke-linecap "round"}))))))
       ;; HTML layer: dots as absolutely-positioned divs (fixed pixel size).
       (for [{:keys [degree color positions]} chord-data]
         ($ :div {:key degree :class "caged-dot-group"}
            (for [{:keys [string-idx fret note]} positions]
              ($ :div {:key (str string-idx "-" fret)
                       :class "caged-dot"
                       :style {:left (pct (+ fret 0.5) fb/num-frets)
                               :top (pct (+ string-idx 0.5) 6)
                               :background-color color}}
                 note)))))))

(defui caged-legend [{:keys [selected-key minor-notation active-degrees on-toggle]}]
  (let [chords (theory/get-diatonic-chords selected-key)]
    ($ Group {:gap "xs" :justify "center" :mb "sm"}
       (for [{:keys [degree root quality]} chords
             :let [active? (contains? active-degrees degree)]]
         ($ :button {:key degree
                     :class (str "caged-legend-item" (when-not active? " inactive"))
                     :on-click #(on-toggle degree)}
            ($ :div {:class "caged-legend-swatch"
                     :style {:background-color (get theory/diatonic-colors degree)}})
            ($ :span {:class "caged-legend-degree"} degree)
            ($ :span {:class "caged-legend-name"}
               (render-chord-name root quality minor-notation)))))))

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

(defui diatonic-chords-section [{:keys [selected-key minor-notation]}]
  (let [chords (theory/get-diatonic-chords selected-key)]
    ($ :div {:class "diatonic-chords"}
       ($ :div {:class "diatonic-title"}
          (str "Chords in the key of " selected-key " major"))
       ($ :div {:class "diatonic-list"}
          (for [{:keys [degree root quality]} chords
                :let [quality-class (-> quality
                                        (.toLowerCase)
                                        (.replace " " "-"))]]
            ($ :div {:key degree
                     :class (str "diatonic-chord quality-" quality-class)}
               ($ :div {:class "diatonic-degree"} degree)
               ($ :div {:class "diatonic-name"}
                  (render-chord-name root quality minor-notation))
               ($ :div {:class "diatonic-quality"} quality)))))))

(defui settings-modal [{:keys [opened settings on-update on-close]}]
  ($ Modal {:opened (boolean opened)
            :onClose on-close
            :title "Settings"
            :centered true
            :size "md"}
     ($ :div {:style {:padding "8px 0"}}
        ($ :div {:style {:margin-bottom "16px"}}
           ($ :div {:style {:font-size "14px" :font-weight 500 :margin-bottom "6px"}}
              "Minor chord notation")
           ($ SegmentedControl
              {:value (:minor-notation settings)
               :onChange #(on-update :minor-notation %)
               :data #js [#js {:value "m" :label "Fm"}
                          #js {:value "-" :label "F⁻"}]}))
        ($ :div
           ($ :div {:style {:font-size "14px" :font-weight 500 :margin-bottom "6px"}}
              "Fretboard display")
           ($ SegmentedControl
              {:value (:fretboard-mode settings)
               :onChange #(on-update :fretboard-mode %)
               :data #js [#js {:value "notes" :label "Notes"}
                          #js {:value "caged" :label "Triads"}]})))))

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

(defn- deg->rad [d] (* d (/ Math/PI 180)))

(defn- polar [cx cy r angle-deg]
  (let [a (deg->rad angle-deg)]
    [(+ cx (* r (Math/cos a)))
     (+ cy (* r (Math/sin a)))]))

(defn- wedge-path [cx cy r-inner r-outer a-start a-end]
  (let [[x1i y1i] (polar cx cy r-inner a-start)
        [x2i y2i] (polar cx cy r-inner a-end)
        [x1o y1o] (polar cx cy r-outer a-start)
        [x2o y2o] (polar cx cy r-outer a-end)]
    (str "M " x1i "," y1i
         " A " r-inner " " r-inner " 0 0 1 " x2i "," y2i
         " L " x2o "," y2o
         " A " r-outer " " r-outer " 0 0 0 " x1o "," y1o
         " Z")))

(defui key-wheel [{:keys [selected-key on-select]}]
  (let [cx 200 cy 200
        r-inner 55
        r-mid 115
        r-outer 180]
    ($ :div {:class "key-wheel-wrap"}
       ($ :svg {:class "key-wheel" :view-box "0 0 400 400"}
          (for [[idx note] (map-indexed vector theory/circle-of-fourths)
                :let [a-center (+ -90 (* idx 30))
                      a-start (- a-center 15)
                      a-end (+ a-center 15)
                      hue (* idx 30)
                      [tx-out ty-out] (polar cx cy (/ (+ r-mid r-outer) 2) a-center)
                      [tx-in ty-in] (polar cx cy (/ (+ r-inner r-mid) 2) a-center)
                      selected? (= note selected-key)
                      minor-root (get theory/relative-minor-display note)]]
            ($ :g {:key note}
               ;; Outer (major) wedge
               ($ :path {:d (wedge-path cx cy r-mid r-outer a-start a-end)
                         :class (str "key-wedge" (when selected? " selected"))
                         :style {:fill (str "hsl(" hue " 70% " (if selected? "55%" "82%") ")")}
                         :on-click #(on-select note)})
               ;; Inner (relative minor) wedge
               ($ :path {:d (wedge-path cx cy r-inner r-mid a-start a-end)
                         :class (str "key-wedge" (when selected? " selected"))
                         :style {:fill (str "hsl(" hue " 50% " (if selected? "65%" "88%") ")")}
                         :on-click #(on-select note)})
               ;; Major label
               ($ :text {:x tx-out :y ty-out
                         :text-anchor "middle"
                         :dominant-baseline "central"
                         :class (str "key-wedge-label major" (when selected? " selected"))}
                  note)
               ;; Relative minor label
               ($ :text {:x tx-in :y ty-in
                         :text-anchor "middle"
                         :dominant-baseline "central"
                         :class (str "key-wedge-label minor" (when selected? " selected"))}
                  (str minor-root "m"))))))))

(defui app []
  (let [[selected-key set-selected-key] (uix/use-state "C")
        [selected-chord set-selected-chord] (uix/use-state "6th")
        [show-all set-show-all] (uix/use-state false)
        [clicked-notes set-clicked-notes] (uix/use-state {})

        ;; State for chord diagrams
        [selected-chord-type set-selected-chord-type] (uix/use-state "major")
        [active-tab set-active-tab] (uix/use-state "fretboard") ; "fretboard" or "chords"

        ;; Settings (persisted to localStorage)
        [user-settings update-setting] (settings/use-settings)
        [settings-open? set-settings-open] (uix/use-state false)

        handle-note-click (fn [position note]
                            (set-clicked-notes
                             (fn [notes]
                               (if (contains? notes position)
                                 (dissoc notes position)
                                 (assoc notes position note)))))

        handle-clear (fn [] (set-clicked-notes {}))

        handle-chord-type-change (fn [chord-type]
                                   (set-selected-chord-type chord-type))

        handle-toggle-degree (fn [degree]
                               (let [current (or (:active-degrees user-settings) #{})
                                     next-set (if (contains? current degree)
                                                (disj current degree)
                                                (conj current degree))]
                                 (update-setting :active-degrees next-set)))]

    ($ :div {:class "app-container"}
       ($ :div {:class "header"}
          ($ ActionIcon {:variant "default" :size "lg" :radius "xl"
                         :aria-label "Settings"
                         :onClick #(set-settings-open true)}
             "⚙"))

       ($ settings-modal {:opened settings-open?
                          :settings user-settings
                          :on-update update-setting
                          :on-close #(set-settings-open false)})

       ;; Key selector (shared between both tabs)
       ($ :div {:class "shared-controls"}
          ($ :div {:class "control-group"}
             ($ :label {:class "control-label"} "Select Key:")
             ($ key-wheel {:selected-key selected-key
                           :on-select set-selected-key}))
          ($ diatonic-chords-section {:selected-key selected-key
                                      :minor-notation (:minor-notation user-settings)}))

       ;; Compact tab nav (Fretboard view / Chord Diagrams view)
       ($ Tabs {:value active-tab :onChange set-active-tab :variant "pills"
                :mb "md" :mt "xs"}
          ($ (.-List mantine/Tabs) {:justify "center"}
             ($ (.-Tab mantine/Tabs) {:value "fretboard"} "Fretboard")
             ($ (.-Tab mantine/Tabs) {:value "chords"} "Chord Diagrams")))

       ;; Conditional content based on active tab
       (case active-tab
         "fretboard"
         (let [caged-mode? (= "caged" (:fretboard-mode user-settings))]
           ($ :div {:class "fretboard-tab"}
              (when-not caged-mode?
                ($ Stack {:gap "md" :align "center" :mb "md"}
                   ($ :div
                      ($ Text {:size "sm" :fw 500 :ta "center" :mb 8} "Select Chord Type:")
                      ($ Group {:gap "xs" :justify "center"}
                         (for [chord-type (keys theory/chord-formulas)]
                           ($ Button {:key chord-type
                                      :variant (if (= chord-type selected-chord) "filled" "default")
                                      :color "teal"
                                      :size "xs"
                                      :onClick #(set-selected-chord chord-type)}
                              chord-type))))
                   ($ Switch {:checked show-all
                              :onChange #(set-show-all (.. % -currentTarget -checked))
                              :label "Show all notes"
                              :color "orange"})))

              (if caged-mode?
                ($ caged-legend {:selected-key selected-key
                                 :minor-notation (:minor-notation user-settings)
                                 :active-degrees (or (:active-degrees user-settings) #{})
                                 :on-toggle handle-toggle-degree})
                ($ legend {:selected-key selected-key}))

              ($ :div {:class "fretboard-container"}
                 ($ :h2 {:class "fretboard-title"}
                    (if caged-mode?
                      (str "Key of " selected-key " — Triads")
                      (str selected-key selected-chord)))
                 ($ :div {:class "fretboard-stack"}
                    ($ fretboard-component {:selected-key (when-not caged-mode? selected-key)
                                            :selected-chord (when-not caged-mode? selected-chord)
                                            :show-all (and show-all (not caged-mode?))
                                            :clicked-notes (if caged-mode? {} clicked-notes)
                                            :on-note-click handle-note-click})
                    (when caged-mode?
                      ($ caged-overlay {:selected-key selected-key
                                        :active-degrees (or (:active-degrees user-settings) #{})}))))

              (when-not caged-mode?
                ($ chord-identifier {:clicked-notes-map clicked-notes
                                     :on-clear handle-clear}))))

         "chords"
         ($ :div {:class "chords-tab"}
            ($ chord-diagram/chord-shapes-section
               {:selected-key selected-key
                :selected-chord-type selected-chord-type
                :on-chord-type-change handle-chord-type-change}))

         ;; Default fallback
         ($ :div "Loading...")))))

(defonce root
  (uix.dom/create-root (js/document.getElementById "root")))

(defn render []
  (uix.dom/render-root
   ($ MantineProvider
      {:defaultColorScheme "light"
       :cssVariablesSelector ":root"}
      ($ app))
   root))

(defn ^:export init []
  (render))