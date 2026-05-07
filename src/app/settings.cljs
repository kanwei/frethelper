(ns app.settings
  (:require [uix.core :as uix]
            [clojure.edn :as edn]))

(def storage-key "frethelper-settings")

(def default-settings
  {:minor-notation "m"      ; "m" or "-"
   :fretboard-mode "notes"  ; "notes" or "caged"
   :active-variations {}}) ; degree -> variation label; presence = chord active

(defn load-settings []
  (try
    (if-let [raw (.getItem js/localStorage storage-key)]
      (merge default-settings (edn/read-string raw))
      default-settings)
    (catch :default _
      default-settings)))

(defn save-settings [settings]
  (try
    (.setItem js/localStorage storage-key (pr-str settings))
    (catch :default _ nil)))

(defn use-settings []
  (let [[settings set-settings] (uix/use-state (load-settings))
        update-setting (uix/use-callback
                        (fn [k v]
                          (set-settings
                           (fn [s]
                             (let [next (assoc s k v)]
                               (save-settings next)
                               next))))
                        [])]
    [settings update-setting]))
