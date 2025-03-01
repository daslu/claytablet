# Clojure IDE

A simple Clojure-based web IDE that supports browsing, loading, saving, and editing files, as well as evaluating Clojure code through nREPL. The frontend is built as a single-page application using CodeMirror for the editor UI.

## Features

- **File Browser**: Browse and select files to edit.
- **Code Editor**: Edit Clojure code with syntax highlighting.
- **Save Functionality**: Save changes to files.
- **Evaluate Code**: Execute Clojure code and view the output.

## Requirements

- [Clojure CLI Tools](https://clojure.org/guides/getting_started) installed.

## Running the Application

1. **Clone the repository**:
   ```
   git clone https://github.com/yourusername/clojure-ide.git
   cd clojure-ide
   ```

2. **Start the server**:
   ```
   clojure -M:run
   ```

3. **Access the IDE**:
   Open your browser and navigate to `http://localhost:3000`.

## Development

To run the application in development mode with additional dependencies:

