# Shape Paint+ (Android, Kotlin, MVVM)

Shape Paint+ is an Android drawing app built with Kotlin, Fragments, XML layouts, Navigation Component, Room, DataStore, MotionLayout, Retrofit, and Glide. The app focuses on lightweight project-based drawing with shape tools, freehand paint, background image import, local persistence, and editor customization.

## Current Feature Set
- Multi-screen Android app with:
  - `GalleryFragment`
  - `EditorFragment`
  - `SettingsFragment`
  - `ReferenceSearchFragment`
  - `ExportActivity`
- MVVM architecture with ViewModels, repositories, Room entities/DAOs, and DataStore-backed settings
- Saved-project dashboard with:
  - create project
  - open project
  - rename project from editor overflow menu
  - delete project from gallery actions or editor overflow flow
- Drawing editor with:
  - rectangle, square, circle, oval, triangle
  - freehand drawing
  - eraser
  - color selection
  - size and brush controls
  - undo and clear
  - MotionLayout expand/collapse tool panel
- Background image support with:
  - camera capture
  - Openverse image search
  - import selected image as project background
- Explicit save flow:
  - changes are saved only when the user selects `Save`
  - unsaved-change prompt on editor back navigation
- Local persistence:
  - Room stores projects, shapes, and strokes
  - DataStore stores user preferences such as artist name, grid, labels, and default size
- Export summary screen launched with an explicit intent

## Tech Stack
- Kotlin
- Fragments + Navigation Component
- ConstraintLayout + MotionLayout
- RecyclerView
- Room
- DataStore
- Retrofit + Moshi
- Glide
- Camera runtime permission flow

## Project Structure
- `app/src/main/java/com/example/shapepaint/data`
  - app container
  - repositories
  - Room database, DAOs, entities
  - Retrofit service models
- `app/src/main/java/com/example/shapepaint/model`
  - domain models and enums
- `app/src/main/java/com/example/shapepaint/ui`
  - gallery, editor, settings, reference search, export
  - adapters and custom drawing canvas
- `app/src/main/res/layout`
  - XML screen layouts and RecyclerView item layouts
- `app/src/main/res/xml`
  - MotionLayout scenes
- `DESIGN_DOCUMENT.md`
  - application overview, milestones, and rubric alignment

## External API
- Reference image search uses Openverse:
  - search query sent through Retrofit
  - results displayed in a RecyclerView
  - thumbnails loaded with Glide
  - selected image imported as project background

## Hardware Integration
- Camera background capture
- Runtime camera permission request at point of use

## Build And Run
1. Open the project in Android Studio.
2. Let Gradle sync complete.
3. Run the `app` configuration on an emulator or Android device.

Headless build command:
```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" \
GRADLE_USER_HOME="$PWD/.gradle-local" \
./gradlew assembleDebug
```

## Verification Status
- Verified with `assembleDebug`
- Installed and launched on Android emulator `emulator-5554`

## Notes
- Editor changes are not auto-saved.
- Back from the editor prompts to save when there are unsaved changes.
- Shape labels are not drawn on the canvas.
