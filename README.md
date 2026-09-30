# SOS — Student on Study

Android Studio project built with Kotlin and Jetpack Compose. It contains the Login Page, Main Menu, and Dashboard.

## Run

1. Open this folder in Android Studio.
2. Let Gradle sync finish and select an emulator or Android device (API 24+).
3. Run the `app` configuration.

The login is a local UI flow for this assignment. Enter any valid email address and a password of at least six characters to open Main Menu. The Dashboard card or bottom navigation opens Dashboard. The back arrow or Main Menu tab returns to Main Menu, and the logout icon returns to Login.

To build from a terminal on Windows, run `gradlew.bat :app:assembleDebug`. The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.
