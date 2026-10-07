# SOS — Student on Study

Android Studio project built with Kotlin and Jetpack Compose. It uses MVVM, Room, and Navigation Compose, with Login/Register, Home (dashboard), Explore (help requests CRUD), and Profile screens styled after the Figma design. See [PROGRESS.md](PROGRESS.md) for the rubric checklist and the Figma link.

## Run

1. Open this folder in Android Studio.
2. Let Gradle sync finish and select an emulator or Android device (API 24+).
3. Run the `app` configuration.

Log in with the seeded demo account **demo@sos.com / password123**, or register a new account. The session is remembered until you log out.

To build from a terminal on Windows, run `gradlew.bat :app:assembleDebug`. The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.
