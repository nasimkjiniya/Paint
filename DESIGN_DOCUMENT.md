# Shape Paint+ Design Document

## Project Information
**Project title:** Shape Paint+  
**Platform:** Android  
**Implementation stack:** Kotlin, Fragments, XML, MVVM  
**Submission status date:** March 25, 2026

## 1. Project Overview
Shape Paint+ is an Android drawing application designed to support quick visual creation, local project management, and simple background-assisted artwork. The application allows users to create projects, draw with geometric shapes and freehand tools, import background imagery, save work locally, and continue editing across sessions.

The application was designed to satisfy the key development areas required by the project rubric:
- Application Architecture
- UI and Layout
- API Connectivity and Data Persistence
- Hardware Integration
- User-based Functionality

The final product is a multi-screen Android application with a project gallery, drawing editor, settings screen, reference image search, and export summary flow.

## 2. Problem Statement
Many lightweight mobile drawing applications either provide only temporary sketching with no persistence, or they become overly complex for users who need a simple and direct workflow. Shape Paint+ addresses this gap by offering a structured but approachable drawing experience with local persistence, configurable editing behavior, hardware-assisted background capture, and external image search for reference material.

## 3. Intended Users
The application is intended for:
- students learning visual composition or shape-based design
- hobby users who want a simple drawing workspace
- users who want to save and return to projects later
- users who benefit from quick background reference images while drawing

## 4. Application Summary
Shape Paint+ is organized around project-based creation. Users begin on a gallery screen, create or open a project, and then move into an editor screen where they can place shapes, draw freehand strokes, erase content, and import a background from the device camera or an online reference search. Settings are stored locally so user preferences remain available across sessions. Project content is explicitly saved by the user and restored later from local storage.

## 5. Major Features

### 5.1 Application Architecture
- MVVM architecture with clear separation between UI, business logic, and data access
- dedicated ViewModels for gallery, editor, settings, and reference search
- repository layer for project persistence, settings persistence, and external API access
- shared application container for dependency creation

### 5.2 UI and Layout
- multiple distinct screens implemented with the Navigation Component
- ConstraintLayout-based XML layouts for core screens
- RecyclerView-based project and reference search lists
- MotionLayout animation for the editor tool tray
- portrait and landscape support for key screens

### 5.3 API Connectivity and Data Persistence
- external image search using Openverse through Retrofit and Moshi
- Glide for asynchronous image loading
- Room database for projects, shapes, and strokes
- DataStore for user settings and preferences
- local persistence across app relaunches

### 5.4 Hardware Integration
- camera capture flow for importing a background image
- runtime camera permission request at time of use
- camera feature accessed only after permission is granted

### 5.5 User-based Functionality
- create, open, rename, and delete projects
- draw shapes: square, rectangle, circle, oval, and triangle
- freehand drawing and eraser tool
- explicit save behavior
- unsaved-change confirmation when leaving the editor
- export summary screen via explicit intent

## 6. Minor and Support Features
- adjustable size and brush controls
- color selection controls
- undo and clear actions
- empty-state and loading-state support
- customizable settings for artist name, grid visibility, labels, and default size
- project summary export activity
- background search result import from external API

## 7. Screen Inventory

### GalleryFragment
Purpose:
- provide the project entry point
- display saved projects
- allow creation and project access

Key functions:
- create project
- open project
- delete project
- navigate to settings

### EditorFragment
Purpose:
- provide the main drawing workspace

Key functions:
- place geometric shapes
- draw freehand strokes
- erase content
- change color and sizing controls
- import background from camera
- open background/reference search
- save, rename, delete, and export
- warn user before leaving with unsaved changes

### SettingsFragment
Purpose:
- manage persistent user preferences

Key functions:
- update artist name
- toggle grid visibility
- toggle label preference
- set default size

### ReferenceSearchFragment
Purpose:
- provide external image search for background/reference selection

Key functions:
- search Openverse
- display remote thumbnails with Glide
- import selected image as project background

### ExportActivity
Purpose:
- display project metadata passed by explicit intent

Key functions:
- show summary details
- share summary text

## 8. Technical Architecture Overview

### UI Layer
The UI layer is composed of Fragments and one Activity. Each screen renders ViewModel state and forwards user actions into business logic. RecyclerView adapters are used for project listings and remote reference results.

### ViewModel Layer
ViewModels manage screen state, business actions, and repository interaction. They expose observable state to the UI and handle user-driven events such as saving, searching, importing, and deleting.

### Data Layer
The data layer contains:
- Room entities, DAOs, and the application database
- DataStore-backed settings persistence
- Retrofit service interfaces and response models
- repository implementations that coordinate persistence and API calls

### Data Flow
1. A user action begins in a Fragment.
2. The Fragment sends that action to a ViewModel.
3. The ViewModel updates in-memory state or delegates to a repository.
4. The repository performs local storage or network operations.
5. The ViewModel publishes updated state back to the UI.

## 9. Milestone Schedule
Project timeline: **January 6, 2026 through March 25, 2026**

| Milestone | Dates | Planned Delivery | Expected Outcome |
|---|---|---|---|
| Milestone 1: Design and Foundation | January 6 - January 19, 2026 | Finalize concept, architecture, navigation structure, and base screen layouts | Design document completed, project structure established, app skeleton compiling |
| Milestone 2: Minimal Viable Product | January 20 - February 9, 2026 | Implement gallery, editor, settings, and basic local project workflow | User can create, open, edit, and save a simple project |
| Milestone 3: Core Feature Expansion | February 10 - March 2, 2026 | Add freehand drawing, eraser, deletion, rename flow, export path, and camera integration | Core editing and project management features are complete and stable |
| Milestone 4: API Integration and Final Refinement | March 3 - March 25, 2026 | Add Openverse search, Glide integration, unsaved-change protection, and final layout polish | Application is submission-ready by March 25, 2026 |

## 10. Rubric Alignment

| Rubric Area | Shape Paint+ Implementation |
|---|---|
| Application Architecture | MVVM structure, repositories, ViewModels, Room, DataStore, clear separation of concerns |
| UI and Layout | Multiple screens, Navigation Component, ConstraintLayout, RecyclerView, MotionLayout, portrait/landscape layouts |
| API Connectivity and Data Persistence | Openverse API via Retrofit, Glide image loading, Room persistence, DataStore settings |
| Hardware Integration | Camera integration with runtime permission request |
| User-based Functionality | Project management, drawing tools, background import, export flow, unsaved-change confirmation |

## 11. Requirement Traceability

| Submission Requirement | Implementation Evidence |
|---|---|
| At least three screens with navigation | Gallery, Editor, Settings, Reference Search |
| Explicit intent usage | ExportActivity launched from editor |
| Bundle-based data passing | Project ID passed through navigation arguments |
| RecyclerView and ViewHolder usage | Saved project list and reference result list |
| MotionLayout usage | Animated expand/collapse editor tool tray |
| External data source usage | Openverse image search integration |
| Async remote image loading | Glide thumbnail rendering |
| Local device persistence | Room database and DataStore |
| MVVM usage | ViewModels for UI logic and repositories for data access |
| Lifecycle and system event handling | SavedStateHandle, explicit save flow, unsaved-change back confirmation |
| Hardware functionality | Camera capture with runtime permission handling |

## 12. Testing and Verification Plan
Testing for the application focuses on feature coverage and rubric evidence.

### Build Verification
- Gradle debug build verification

### Manual Verification Areas
- project creation and opening
- project save and reload
- rename and delete behavior
- freehand and eraser behavior
- unsaved-change prompt on back navigation
- camera permission grant and deny flows
- Openverse search and background import
- portrait and landscape layout behavior
- saved-project restoration after app restart

## 13. Submission Readiness Statement
As of March 25, 2026, Shape Paint+ includes the core technical and functional areas required by the rubric:
- structured Android architecture
- multi-screen UI and navigation
- external API integration
- local persistence
- hardware integration
- user-facing project workflows

The application is positioned as a rubric-aligned final submission with supporting documentation, milestone planning, and feature traceability.
