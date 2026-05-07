(ns app.core
  (:require [uix.core :as uix :refer [defui $]]
            [uix.dom]
            [clojure.string :as str]
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
       ($ :div {:class "fret-numbers"}
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
                                      :on-note-click on-note-click}))))))))))

(defn render-chord-name [root quality minor-notation]
  (case quality
    "Minor"      (if (= minor-notation "-")
                   ($ :span root ($ :sup {:class "minor-sup"} "-"))
                   ($ :span root "m"))
    "Dominant 7" ($ :span root "7")
    "Diminished" ($ :span root "°")
    ($ :span root)))

(defn caged-positions [chord-root quality]
  ;; Returns a vector of voicings, each a vector of {:string-idx :fret :note} maps,
  ;; in fretboard string convention (0 = high E, 5 = low E).
  (->> (theory/caged-triad-voicings chord-root quality)
       (mapv (fn [voicing]
               (->> voicing
                    (map (fn [{:keys [string fret]}]
                           (let [fb-idx (- 5 string)
                                 open-note (nth fb/standard-tuning fb-idx)]
                             {:string-idx fb-idx
                              :fret fret
                              :note (fb/get-note-at-fret open-note fret)})))
                    (sort-by :string-idx)
                    (vec))))))

(def inversion-offset
  ;; Tiny y-offset per inversion so overlapping voicings are visually distinct.
  {0 -0.07
   1 0
   2 0.07})

(def inversion-shade
  ;; Lightness adjustment per inversion (positive = lighter, negative = darker).
  {0 0.30
   1 0
   2 -0.25})

(defui caged-overlay [{:keys [selected-key active-degrees active-inversions]}]
  (let [chords (theory/get-diatonic-chords selected-key)]
    ($ :svg {:class "caged-overlay"
             :view-box (str "0 0 " fb/num-frets " 6")
             :preserve-aspect-ratio "none"}
       (for [{:keys [degree root quality]} chords
             :let [voicings (caged-positions root quality)
                   base-color (get theory/diatonic-colors degree "#666")]
             :when (and (seq voicings) (contains? active-degrees degree))]
         ($ :g {:key degree}
            (for [[idx voicing] (map-indexed vector voicings)
                  :when (and (seq voicing) (contains? active-inversions idx))
                  :let [dy (get inversion-offset idx 0)
                        color (theory/lighten base-color (get inversion-shade idx 0))]]
              ($ :g {:key idx}
                 (when (>= (count voicing) 2)
                   ($ :polyline {:points (str/join " "
                                                   (for [{:keys [string-idx fret]} voicing]
                                                     (str (+ fret 0.5) "," (+ string-idx 0.5 dy))))
                                 :fill "none"
                                 :stroke color
                                 :stroke-width 0.06
                                 :stroke-linecap "round"
                                 :stroke-linejoin "round"
                                 :opacity 0.85}))
                 (for [{:keys [string-idx fret note]} voicing]
                   ($ :g {:key (str string-idx "-" fret)}
                      ($ :circle {:cx (+ fret 0.5)
                                  :cy (+ string-idx 0.5 dy)
                                  :r 0.26
                                  :fill color
                                  :stroke "white"
                                  :stroke-width 0.04})
                      ($ :text {:x (+ fret 0.5)
                                :y (+ string-idx 0.5 dy)
                                :text-anchor "middle"
                                :dominant-baseline "central"
                                :font-size 0.28
                                :font-weight "700"
                                :fill "white"
                                :class "caged-note-label"}
                         note))))))))))

(defui inversion-toggles [{:keys [active-inversions on-toggle]}]
  ($ :div {:class "inversion-toggles"}
     (for [[idx label] [[0 "Root"] [1 "1st inv"] [2 "2nd inv"]]]
       (let [active? (contains? active-inversions idx)]
         ($ :label {:key idx
                    :class (str "inversion-toggle" (when-not active? " inactive"))}
            ($ :input {:type "checkbox"
                       :checked active?
                       :on-change #(on-toggle idx)})
            ($ :span label))))))

(defui caged-legend [{:keys [selected-key minor-notation active-degrees on-toggle]}]
  (let [chords (theory/get-diatonic-chords selected-key)]
    ($ :div {:class "caged-legend"}
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

(defui settings-modal [{:keys [settings on-update on-close]}]
  ($ :div {:class "modal-overlay" :on-click on-close}
     ($ :div {:class "modal" :on-click #(.stopPropagation %)}
        ($ :div {:class "modal-header"}
           ($ :h2 {:class "modal-title"} "Settings")
           ($ :button {:class "modal-close" :on-click on-close} "×"))
        ($ :div {:class "modal-body"}
           ($ :div {:class "setting-row"}
              ($ :label {:class "setting-label"} "Minor chord notation")
              ($ :div {:class "setting-options"}
                 ($ :button {:class (str "setting-option"
                                         (when (= "m" (:minor-notation settings)) " selected"))
                             :on-click #(on-update :minor-notation "m")}
                    ($ :span "F") ($ :span "m"))
                 ($ :button {:class (str "setting-option"
                                         (when (= "-" (:minor-notation settings)) " selected"))
                             :on-click #(on-update :minor-notation "-")}
                    ($ :span "F") ($ :sup {:class "minor-sup"} "-"))))
           ($ :div {:class "setting-row"}
              ($ :label {:class "setting-label"} "Fretboard display")
              ($ :div {:class "setting-options"}
                 ($ :button {:class (str "setting-option"
                                         (when (= "notes" (:fretboard-mode settings)) " selected"))
                             :on-click #(on-update :fretboard-mode "notes")}
                    "Notes")
                 ($ :button {:class (str "setting-option"
                                         (when (= "caged" (:fretboard-mode settings)) " selected"))
                             :on-click #(on-update :fretboard-mode "caged")}
                    "CAGED lines")))))))

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
                                 (update-setting :active-degrees next-set)))

        handle-toggle-inversion (fn [inv-idx]
                                  (let [current (or (:active-inversions user-settings) #{})
                                        next-set (if (contains? current inv-idx)
                                                   (disj current inv-idx)
                                                   (conj current inv-idx))]
                                    (update-setting :active-inversions next-set)))]

    ($ :div {:class "app-container"}
       ($ :div {:class "header"}
          ($ :button {:class "settings-button"
                      :title "Settings"
                      :on-click #(set-settings-open true)}
             "⚙"))

       (when settings-open?
         ($ settings-modal {:settings user-settings
                            :on-update update-setting
                            :on-close #(set-settings-open false)}))

       ;; Tab Navigation
       ($ :div {:class "tab-navigation"}
          ($ :button {:class (str "tab-button" (when (= active-tab "fretboard") " active"))
                      :on-click #(set-active-tab "fretboard")}
             "🎼 Fretboard Explorer")
          ($ :button {:class (str "tab-button" (when (= active-tab "chords") " active"))
                      :on-click #(set-active-tab "chords")}
             "📖 Chord Diagrams"))

       ;; Key selector (shared between both tabs)
       ($ :div {:class "shared-controls"}
          ($ :div {:class "control-group"}
             ($ :label {:class "control-label"} "Select Key:")
             ($ :div {:class "button-group"}
                (for [note theory/notes]
                  ($ :button {:key note
                              :class (str "key-button"
                                          (when (= note selected-key) " selected"))
                              :on-click #(set-selected-key note)}
                     note))))
          ($ diatonic-chords-section {:selected-key selected-key
                                      :minor-notation (:minor-notation user-settings)}))

       ;; Conditional content based on active tab
       (case active-tab
         "fretboard"
         (let [caged-mode? (= "caged" (:fretboard-mode user-settings))]
           ($ :div {:class "fretboard-tab"}
              (when-not caged-mode?
                ($ :div {:class "controls"}
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
                      (if show-all "Show Chord Notes Only" "Show All Notes"))))

              (if caged-mode?
                ($ :<>
                   ($ caged-legend {:selected-key selected-key
                                    :minor-notation (:minor-notation user-settings)
                                    :active-degrees (or (:active-degrees user-settings) #{})
                                    :on-toggle handle-toggle-degree})
                   ($ inversion-toggles {:active-inversions (or (:active-inversions user-settings) #{})
                                         :on-toggle handle-toggle-inversion}))
                ($ legend {:selected-key selected-key}))

              ($ :div {:class "fretboard-container"}
                 ($ :h2 {:class "fretboard-title"}
                    (if caged-mode?
                      (str "Key of " selected-key " — CAGED shapes")
                      (str selected-key selected-chord)))
                 ($ :div {:class "fretboard-stack"}
                    ($ fretboard-component {:selected-key (when-not caged-mode? selected-key)
                                            :selected-chord (when-not caged-mode? selected-chord)
                                            :show-all (and show-all (not caged-mode?))
                                            :clicked-notes (if caged-mode? {} clicked-notes)
                                            :on-note-click handle-note-click})
                    (when caged-mode?
                      ($ caged-overlay {:selected-key selected-key
                                        :active-degrees (or (:active-degrees user-settings) #{})
                                        :active-inversions (or (:active-inversions user-settings) #{})}))))

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
   ($ uix/strict-mode
      ($ app))
   root))

(defn ^:export init []
  (render))