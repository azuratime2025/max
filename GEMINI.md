# Project Azura

## Project Overview

This project appears to be a face recognition-based attendance system for Android. It includes features for:

*   Face registration and management
*   Bulk registration from a CSV file
*   Real-time face detection and recognition
*   Check-in functionality
*   Options management for classes, grades, etc.

## Build Instructions

To build the project, run the following command from the root directory:

```bash
./gradlew :app:assembleDebug
```

## Completed Tasks

*   **Fixed Compilation Errors:** A series of compilation errors were fixed in the following files:
    *   `app/src/main/java/com/example/crashcourse/ui/edit/EditUserScreen.kt`
    *   `app/src/main/java/com/example/crashcourse/ui/components/FaceOverlay2.kt`
    *   `app/src/main/java/com/example/crashcourse/ui/face/FaceListScreen.kt`
    *   `app/src/main/java/com/example/crashcourse/ui/MainScreen.kt`
    *   `app/src/main/java/com/example/crashcourse/ui/add/FaceCaptureScreen.kt`

*   **Fixes included:**
    *   Removing extra curly braces
    *   Deleting a duplicate file (`FaceOverlay2.kt`)
    *   Fixing incorrect function calls
    *   Removing unexpected parameters
    *   Removed `val` keyword from function parameters.
    *   Corrected `viewModel` parameter passing in composable functions.
    *   Added missing `NavController` import.
