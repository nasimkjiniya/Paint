# Shape Paint+ Design Document

## 1) Application Overview
**Project name:** Shape Paint+  
**Platform:** Android (Kotlin, XML/Fragments, MVVM)  
**Goal:** Deliver a market-ready drawing application where users create shape-based artwork, enrich it with remote visual assets, save work locally/offline, and use device hardware for advanced creation workflows.

### Problem Statement
Beginner drawing apps are often either too limited (no persistence, no templates) or too complex for quick creative use. Shape Paint+ focuses on fast creation, reliable offline access, and a clear UX that scales from first-time users to repeat users.

### Target Users
- Students and hobbyists who want quick visual sketching.
- Users with intermittent connectivity who still need access to saved projects.
- Users who want basic personalization, project history, and reusable templates.

## 2) Rubric-Aligned Feature Set

## A. Android UI/UX
### Major Features
- **Three+ navigable screens (Navigation Controller + explicit intent):**
  - `GalleryFragment`: list of saved projects and remote templates.
  - `EditorFragment`: drawing canvas and editing tools.
  - `SettingsFragment`: preferences, theme, export options.
  - `ExportActivity` launched via explicit intent from `EditorFragment`.
- **Bundle-based data passing:**
  - Project ID and mode (`new/edit/template`) passed via Safe Args bundle.
- **ConstraintLayout-based UI:**
  - Core screen layouts built with `ConstraintLayout` and flat hierarchy.
- **Data collections via RecyclerView + ViewHolder:**
  - Gallery project list and template list rendered with RecyclerView adapters.
- **MotionLayout animation:**
  - Tool panel expand/collapse and contextual controls animated via MotionScene.

### Minor Features
- Adaptive layouts for phone/tablet using qualifiers.
- Resource organization in `res/values` (`strings`, `colors`, `dimens`, `styles`), `drawable`, and `layout`.
- Empty-state and loading-state UI for first-time and offline scenarios.

## B. Local and Network Data
### Major Features
- **Remote API connectivity (Retrofit + Moshi):**
  - Fetch drawing template metadata and palette packs from a public REST API.
- **Asynchronous image loading (Glide):**
  - Load template thumbnails with placeholders and error drawables.
- **Local persistence:**
  - `Room` database for projects, shapes, recent templates.
  - `DataStore` for user preferences (theme, default shape/color, grid visibility).

### Minor Features
- Repository layer with explicit mapping between DTOs and domain models.
- Offline-first behavior: cached templates shown when network unavailable.
- Coroutines + `Dispatchers.IO` for network/database operations.

## C. Android System and Hardware Integration
### Major Features
- **MVVM architecture:**
  - Fragments/Activities for UI,
  - ViewModels for UI state + business logic,
  - Models/entities for domain and storage.
- **Lifecycle/event handling:**
  - `SavedStateHandle` + `onSaveInstanceState` for draft recovery.
  - Resume-safe behavior for app switching and orientation changes.
  - Intent handling for import/export flows.
- **Hardware integration:**
  - Camera integration to capture a background image for a drawing.
  - Runtime camera permission requested at point-of-use.
  - Camera functionality enabled only after permission granted.

### Minor Features
- Notification for export completion (optional stretch feature).
- Haptic feedback on key drawing actions (optional stretch feature).

## 3) Architecture Overview
### Layers
- **UI Layer:** Activities/Fragments, RecyclerView adapters, MotionLayout scenes.
- **Domain Layer:** Use cases (`CreateProject`, `SaveProject`, `LoadTemplates`).
- **Data Layer:** Retrofit service, Room DAOs, DataStore manager, repository implementations.

### Data Flow
1. UI action is emitted in Fragment.
2. ViewModel processes action and calls use case/repository.
3. Repository resolves from local cache and/or network.
4. ViewModel publishes observable state (LiveData/StateFlow).
5. UI renders state and one-time events (navigation/snackbar/dialog).

## 4) Screen Inventory
- **GalleryFragment**
  - RecyclerView of local projects.
  - RecyclerView section of remote templates with Glide thumbnails.
  - FAB to create new project.
- **EditorFragment**
  - Canvas area for shape placement/editing.
  - Tool controls (shape, color, size, undo/redo).
  - MotionLayout animated tool tray.
  - Buttons for Save, Import from Camera, Export.
- **SettingsFragment**
  - Theme selection, default tool options, cache controls.
  - DataStore-backed preferences.
- **ExportActivity**
  - Receives project ID via explicit intent extras.
  - Generates share/export output and result status.

## 5) Milestone Schedule and Delivery Targets
Assumed project window: **March 30, 2026 to May 24, 2026**

| Milestone | Dates | Scope | Exit Criteria |
|---|---|---|---|
| M1: Design + Foundation | Mar 30 - Apr 5, 2026 | Finalize architecture, nav graph, data models, screen wireframes, repository contracts | Design doc approved; project skeleton compiles; navigation scaffolding in place |
| M2: MVP Proof of Concept | Apr 6 - Apr 19, 2026 | Implement Gallery + Editor + Settings shells, basic drawing flow, ViewModel state, RecyclerView lists, basic Room save/load | App has 3+ screens; user can create and save/load simple projects; nav and bundles working |
| M3: Core Feature Expansion | Apr 20 - May 10, 2026 | Retrofit API integration, Glide async loading, robust local caching, orientation/process recovery, explicit intent export flow | Network templates load with placeholder/error states; offline cache works; lifecycle recovery validated |
| M4: Release Readiness + Polish | May 11 - May 24, 2026 | MotionLayout animations, camera hardware integration + permissions, accessibility polish, QA/regression fixes | MotionLayout scene active; camera import works with runtime permission; release candidate build stable |

## 6) Rubric Traceability Matrix

| Rubric Requirement | Planned Implementation in Shape Paint+ | Evidence for Submission |
|---|---|---|
| 3+ screens with navigation | Gallery, Editor, Settings via Navigation Controller | Nav graph XML + demo video |
| Explicit intent usage | Editor launches ExportActivity with project bundle | Intent code + exported result flow |
| Data passed between screens | Safe Args/Bundle: projectId, templateId, mode | Fragment args classes + logs/screenshots |
| ConstraintLayout usage | Primary XML layouts built with ConstraintLayout; constrained IDs on each view | Layout XML files |
| Data collections with ViewHolder | RecyclerView adapters for projects/templates | Adapter/ViewHolder classes |
| MotionLayout feature | Tool tray transition in Editor MotionScene | MotionScene XML + screen capture |
| External API connectivity | Retrofit + Moshi for templates/palettes | Service interface + repository tests |
| Async network + image loading | Coroutines + Glide placeholders/error states | Loading/error UI states video |
| Local persistence across sessions | Room entities/DAO + DataStore preferences | DB schema + persistence demo |
| MVVM architecture | Fragment/Activity UI, ViewModel logic, model/domain separation | Package structure + architecture diagram |
| Lifecycle/system event handling | SavedStateHandle, onSaveInstanceState, intent and permission result handling | Orientation/process recreation test notes |
| Hardware feature + permissions | Camera capture background import with runtime permission | Permission flow demo + feature video |

## 7) Testing and Quality Plan
- Unit tests for ViewModels, repositories, and model mapping.
- DAO tests for Room read/write and conflict behavior.
- Instrumentation tests for navigation and key UI flows.
- Manual test matrix:
  - Online/offline transitions,
  - orientation changes during editing,
  - permission grant/deny for camera,
  - cold start restoration of last project.

## 8) Submission Package Checklist
- Application source code.
- Buildable Android project and release artifact (APK/AAB as required).
- This design document with milestone schedule.
- Rubric traceability section (Section 6) showing where each criterion is met.
- Short demo video/screenshots showing:
  - multi-screen navigation,
  - MotionLayout animation,
  - API data loading with placeholders,
  - local persistence after relaunch,
  - camera integration with permission handling.
