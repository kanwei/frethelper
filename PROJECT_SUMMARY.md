# FretHelper Project Summary

## Overview
FretHelper is a ClojureScript web application for guitar chord visualization and learning. Built with UIx (React wrapper for ClojureScript), it's designed to help guitar players visualize and understand chord patterns on the fretboard. Currently in early development with basic UIx setup complete.

## Key Technologies
- **ClojureScript** (1.12.42) - Core language
- **UIx** (1.4.4) - Modern React wrapper for ClojureScript with hooks support
- **Shadow-CLJS** (3.2.0) - Build tool and hot-reload development server
- **React** (19.1.0) - UI framework (via UIx)
- **LightningCSS** - CSS bundling and minification

## Project Structure

### Core Files
- `/src/app/core.cljs` - Main application entry point with UIx component setup
- `/deps.edn` - Clojure dependencies configuration
- `/shadow-cljs.edn` - Shadow-CLJS build configuration
- `/package.json` - NPM dependencies and build scripts
- `/public/index.html` - HTML entry point
- `/src/app/core.css` - Base CSS styles

### Build Output
- `/public/js/main.js` - Compiled JavaScript bundle
- `/public/main.css` - Bundled CSS output
- `/public/js/cljs-runtime/` - ClojureScript runtime files (development)

## Dependencies

### Clojure/ClojureScript
```clojure
{:deps {org.clojure/clojure {:mvn/version "1.12.1"}
        org.clojure/clojurescript {:mvn/version "1.12.42"}
        com.pitch/uix.core {:mvn/version "1.4.4"}
        com.pitch/uix.dom {:mvn/version "1.4.4"}}}
```

### Development Dependencies
- `shadow-cljs` (3.2.0) - ClojureScript build tool
- `devtools` (1.0.7) - Browser DevTools formatters for ClojureScript
- `react-refresh` (0.17.0) - Fast refresh for React components
- `lightningcss-cli` - CSS processing
- `onchange` - File watcher for CSS

## Architecture

### Component System
Uses UIx's modern React wrapper with:
- `defui` macro for component definitions
- `$` helper for creating React elements
- Hooks support (useState, useEffect, etc.)
- StrictMode enabled in development

### Build Pipeline
1. **Shadow-CLJS** compiles ClojureScript to JavaScript
2. **LightningCSS** processes and bundles CSS files
3. **Development server** on port 8700 with hot-reload
4. **REPL** available on port 62000

### Current Application Structure
```clojure
(ns app.core
  (:require [uix.core :as uix :refer [defui $]]
            [uix.dom]))

(defui app []
  ($ :h1 "Hello, UIx!"))  ; Main component - ready for guitar fretboard implementation
```

## Development Workflow

### Initial Setup
```bash
npm install  # Install Node dependencies
```

### Development Mode
```bash
npm run dev  # Starts Shadow-CLJS watcher and CSS watcher
```
This command:
- Starts Shadow-CLJS in watch mode
- Opens dev server at http://localhost:8700
- Enables hot-reload for both ClojureScript and CSS
- Provides REPL on port 62000

### Production Build
```bash
npm run release  # Creates optimized production bundle
```

### REPL Connection
```clojure
;; Connect to nREPL on port 62000
;; Then switch to browser REPL:
(shadow/repl :app)
```

## Implementation Patterns

### UIx Component Pattern
```clojure
(defui component-name [props]
  ;; Use hooks here
  (let [state (uix/use-state initial-value)]
    ;; Return React elements
    ($ :div
       ($ :h1 "Title")
       ($ another-component {:prop value}))))
```

### CSS Organization
- Component styles in `/src/app/[component].css`
- Auto-bundled to `/public/main.css`
- LightningCSS handles bundling and minification

## Extension Points

### Planned Features
1. **Fretboard Component** - Interactive guitar fretboard visualization
2. **Chord Library** - Database of chord shapes and fingerings
3. **Scale Visualization** - Display scales on the fretboard
4. **Audio Integration** - Play chord sounds
5. **Chord Progression** - Sequence and practice chord changes

### Component Structure (Proposed)
```
app.core (main app)
├── app.fretboard (fretboard display)
├── app.chord-library (chord database)
├── app.audio (sound generation)
└── app.ui (shared UI components)
```

### State Management Options
- Local component state with `use-state`
- Global state with Re-frame (add via `--re-frame` flag)
- Context API via UIx hooks

## Available NPM Scripts
- `npm run dev` - Development build with watchers
- `npm run release` - Production build
- `npm run styles-dev` - CSS watcher (part of dev)
- `npm run styles-release` - CSS production build

## Development Tips
1. Use Shadow-CLJS dashboard at http://localhost:9630 for build insights
2. Browser DevTools will show formatted ClojureScript data structures
3. Hot-reload preserves component state during development
4. Use `uix.preload` for development-time setup
5. CSS changes auto-refresh without losing app state

## Future Considerations
- Add Re-frame for complex state management
- Consider adding routing with reitit
- Integrate Tone.js for audio synthesis
- Add PWA capabilities for offline use
- Consider adding MIDI support for external controllers