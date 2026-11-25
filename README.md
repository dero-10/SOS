# SOS — Student on Study

Android Studio project built with Kotlin and Jetpack Compose. It uses MVVM, Room, and Navigation Compose, with Login/Register, Home (dashboard), Explore (requests and helpers), Chat, Recent, request detail with replies, helper profiles, and Profile screens styled after the Figma design. See [PROGRESS.md](PROGRESS.md) for the rubric checklist and the Figma link.

## Run

1. Open this folder in Android Studio.
2. Let Gradle sync finish and select an emulator or Android device (API 24+).
3. Run the `app` configuration.

Log in with the seeded demo account **demo@sos.com / password123**, or register a new account. The session is remembered until you log out.

To build from a terminal on Windows, run `gradlew.bat :app:assembleDebug`. The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Online mode (Supabase) — branch `online-supabase`

Without Supabase keys the app runs fully offline on Room, exactly as above. To use the online backend:

1. Create a project at [supabase.com](https://supabase.com) (free tier is enough).
2. Dashboard → **SQL Editor** → New query → paste [`supabase/schema.sql`](supabase/schema.sql) → **Run**.
3. For development, Dashboard → **Authentication → Sign In / Providers → Email** → turn off **Confirm email** (otherwise new accounts must click the emailed link before logging in).
4. Dashboard → **Project Settings → API**, copy the **Project URL** and the **anon / publishable** key into `local.properties` (it is git-ignored, never commit it):
   ```
   supabase.url=https://<project-ref>.supabase.co
   supabase.anonKey=<anon key>
   ```
5. Rebuild. Login and Register now go to Supabase; the seeded demo account is offline-only, so register a new account.

Never put the `service_role` key in the app.
