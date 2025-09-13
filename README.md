# FretHelper - Interactive Guitar Chord Visualizer

## Overview
FretHelper is an interactive web application that helps guitarists visualize and understand chord shapes on the fretboard. It uses a rainbow color-coding system to show the intervals within chords, making it easy to see chord patterns and relationships across the neck.

## Features

### 🎸 Interactive Chord Visualization
- Select any root note (C through B including sharps/flats)
- Choose from 21 different chord types (Major, Minor, 7ths, 9ths, Diminished, Augmented, etc.)
- See chord notes highlighted on a 12-fret guitar fretboard
- Rainbow color-coding shows intervals:
  - **Red squares**: Root notes (1st)
  - **Orange**: 2nd/9th intervals
  - **Yellow**: 3rd intervals
  - **Green**: 4th/11th intervals  
  - **Blue**: 5th intervals
  - **Purple**: 6th/7th intervals

### 🎵 Chord Identifier
- Click on any visible notes on the fretboard to select them
- Automatically identifies what chord(s) your selected notes form
- Helps you discover chord names from shapes you already know
- Requires at least 3 notes to identify chords

### 🎯 Visual Features
- Realistic string thickness (thicker bass strings, thinner treble strings)
- Toggle between showing only chord notes or all notes on the fretboard
- Selected notes show with a border for clear visual feedback
- Clean, modern interface with gradient background

## Getting Started

### Prerequisites
- Node.js and npm installed
- Modern web browser

### Installation
```bash
# Clone the repository
git clone [repository-url]
cd frethelper

# Install dependencies
npm install
```

### Development
```bash
# Start the development server with hot-reload
npm run dev
```
The app will be available at http://localhost:8700

### Production Build
```bash
# Create optimized production bundle
npm run release
```

## How to Use

1. **Select a Key**: Click on any root note button (C, Db, D, Eb, E, F, Gb, G, Ab, A, Bb, B)

2. **Choose a Chord Type**: Select from various chord types like Major, Minor, 7th, Diminished, etc.

3. **Explore the Fretboard**: 
   - Chord notes automatically appear on the fretboard with color-coded intervals
   - Root notes appear as squares, other intervals as circles
   - Click "Show All Notes" to see every note on the fretboard

4. **Identify Unknown Chords**:
   - Click on individual notes on the fretboard to select them
   - The chord identifier will show you what chord(s) those notes form
   - Click "Clear Selection" to start over

## Technology Stack

- **ClojureScript** - Core programming language
- **UIx** - React wrapper for ClojureScript with hooks support
- **Shadow-CLJS** - Build tool and development server
- **React 19** - UI framework
- **LightningCSS** - CSS bundling and optimization

## Project Structure

```
frethelper/
├── src/
│   └── app/
│       ├── core.cljs         # Main UI components
│       ├── core.css           # Styling
│       ├── music_theory.cljs # Chord formulas and music theory logic
│       └── fretboard.cljs    # Fretboard generation logic
├── public/
│   ├── index.html            # HTML entry point
│   └── js/                   # Compiled JavaScript
├── deps.edn                  # Clojure dependencies
├── shadow-cljs.edn           # Shadow-CLJS configuration
└── package.json              # NPM dependencies and scripts
```

## Music Theory Implementation

The app includes comprehensive music theory logic:
- All 12 chromatic notes with enharmonic equivalents (flats)
- 21 chord formulas including extended and altered chords
- Interval-based color system for visual learning
- Chord identification algorithm that finds all possible chord interpretations

## Contributing

Feel free to submit issues and enhancement requests!

## License

[Your License Here]

## Acknowledgments

Built with ClojureScript and UIx for fast, interactive music education tools.