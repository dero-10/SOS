# SOS — Student on Study: Progress & Rubric Tracker

> Handoff file for humans and AI assistants. Read this first before changing the project.
> Last updated: 2026-10-07

## What the app is

SOS is a peer-help marketplace for students: a student posts a **help request** (details, attachment, estimated time, deadline, budget, tags), helpers reply or chat, and the student pays from an in-app **balance**. The current build covers the parts the UTS rubric grades; Chat, Helpers and Recent are still to do (see "Next phase").

## Design source (Figma)

- **Figma file:** [SOS - Student on Study (Copy)](https://www.figma.com/design/l7TOames4VGmoCETYpcXab/SOS---Student-on-Study--Copy-?node-id=0-1&t=C9oYYLE8wrLuZpFf-1)
- File key: `l7TOames4VGmoCETYpcXab`, root node: `0:1`
- The Figma MCP server has **no access** to this file ("you don't have edit access"), so the user shared screenshots instead. Frames in the file:
  1. **Add Request Details** — Request details textarea, "+ Add Attachment", Estimated time dropdown (⚡ Fast (< 1 hour)), Deadline chips (Today, Tomorrow, 3 days, 1 week, Custom date), Budget stepper (IDR 50,000 ▲▼), purple pill "Send".
  2. **Chat detail** (Chris) — request bubble with attachment (ui ux assignment.pdf), "Request has been sent.", message bar with paperclip + purple send button.
  3. **Chat list** — search, conversations (Chris, Abby, William, Coco, Billy) with time ago.
  4. **Explore → Helper tab** — helper cards: avatar, name + verified shield, Online/Online today pill, ★ rating • N helps, education, skill tags.
  5. **Helper profile** (Chris) — big avatar, rating, education, LinkedIn, stats (23 Helps / 14 Requests / 18 Reviews), Recent Helps cards with stars, Skills chips, "Chat Helper" button.
  6. **Request detail (other user's)** — author, title, category/time/budget meta, description, image, Tags, "Replies (4)" cards with portfolio links, "Post a reply…" bar.
  7. **Explore → Request tab** — search + sort icon, Request/Helper tabs, Filter pill + "Design ×" chip, request cards (title, red Urgent badge, budget, estimated time, reply count, tags).
  8. **Profile** (Patrick Jane) — gradient initial avatar, name + LinkedIn, email, education, "Edit Profile" pill, teal balance card (IDR 250,000, Withdraw, Buy Credits), rows Language/English, Display/Light mode, Logout.
  9. **Request detail (own)** — teal header "Request" with back arrow, same card as #6.
- Bottom nav in Figma: Home, Explore, Chat, Recent, Profile (icons black, selected purple).
- Design tokens are in [`ui/SosTheme.kt`](app/src/main/java/com/sos/studentonstudy/ui/SosTheme.kt): purple `#4F46E5`, background `#F4F4F4`, card `#F7F7F7` + shadow, teal chips `#D4E7E8`, field gray `#EEEDEB`, urgent red. Figma uses the **Inter** font; the app still uses the system font (TODO).

## Grading rubric (UTS — "D. Minimal Project Apps", 45% of total)

Score per criterion is 1–4; criterion value = weight × score ÷ 4. Syllabus reference (Pertemuan 8): Jetpack Compose UI with at least 4 screens, MVVM, Room Database, multi-screen navigation. The other 20% ("Subtotal UI: Full Design") is judged against the Figma.

| Code | Criterion | Weight | "Sangat Baik (4)" requirement | Status | Where |
|---|---|---|---|---|---|
| D1 | Login | 8 | Validation (empty, format), clear error messages, session saved, logout works | ✅ Done | `ui/auth/*`, `data/SessionManager.kt` |
| D2 | Dashboard | 8 | Real summary data from Room, auto-updates via Flow/StateFlow | ✅ Done (Home tab) | `ui/dashboard/*` |
| D3 | Menu & navigation | 7 | Bottom bar to every screen; correct back stack; state survives rotation | ✅ Done | `ui/navigation/SosNavHost.kt` |
| D4 | Profile | 7 | Shows logged-in user data; editable and saved; logout button | ✅ Done | `ui/profile/*` |
| D5 | CRUD module with Room | 10 | Full create/read/update/delete; form validation; persists; delete confirmation | ✅ Done (Requests) | `ui/request/*`, `ui/explore/*`, `data/local/*` |
| D6 | Architecture & code quality | 5 | Consistent MVVM + Repository; UI never touches DAO; tidy Git commits | ✅ Done | whole project |

### How each criterion is met

- **D1 Login** — `LoginViewModel` errors: "Email is required", "Enter a valid email address", "Password is required", "at least 6 characters", plus "No account found…" / "Incorrect password" checked against Room (SHA-256 hash). `SessionManager` (SharedPreferences) keeps the user logged in across restarts. Register screen creates accounts. Logout (Profile → Logout → confirm) clears the session and back stack. Demo account: **demo@sos.com / password123** (Patrick Jane).
- **D2 Dashboard (Home)** — `DashboardViewModel` combines `currentUser` + `requests` Flows: balance, Requests / Urgent / This week counts, 3 most recent requests. Updates live after any create/edit/delete.
- **D3 Navigation** — Navigation Compose. Routes: `login`, `register`, `home`, `explore`, `profile`, `request_form?requestId=`. Bottom bar (Home, Explore, Profile) uses `popUpTo(HOME){saveState}` + `restoreState`; Back from a tab → Home → exit. Auth screens are popped after login; logout pops everything. ViewModels + `rememberSaveable` keep state on rotation.
- **D4 Profile** — Figma layout; name, email, major + university, balance from Room. "Edit Profile" opens an edit form (name required) saved via `AuthRepository.updateProfile`; system Back cancels editing.
- **D5 CRUD (Requests)** — Create: "Post a Request" (Home) or + FAB (Explore) → Add Request Details form. Read: Explore list with search, tag filter, sort (Newest / Highest budget / Nearest deadline), and Home recent list. Update: tap a card → same form in edit mode → Save. Delete: trash icon on the edit screen → confirm dialog. Validation: title ≥5 chars, details ≥10 chars, deadline required, ≥1 tag, budget ≤ balance. Attachment picks a real file (stores display name only).
- **D6 Architecture** — `data/local` (Room) → `data/repository` → `ui/<feature>/<Feature>ViewModel` (StateFlow) → Composables. Manual DI via `SosApplication`/`AppContainer`; all ViewModels built in `ui/SosViewModelFactory.kt`.

## Project structure

```
app/src/main/java/com/sos/studentonstudy/
├── SosApplication.kt          # Application + AppContainer (DB, session, repos, demo seed)
├── MainActivity.kt
├── data/
│   ├── SessionManager.kt
│   ├── local/                 # Entities.kt (UserEntity, RequestEntity, EstimatedTime, RequestTags), Daos.kt, SosDatabase.kt (v2)
│   └── repository/            # AuthRepository.kt, RequestRepository.kt
└── ui/
    ├── SosTheme.kt, Format.kt, UiComponents.kt, SosViewModelFactory.kt
    ├── navigation/SosNavHost.kt
    ├── auth/                  # Login, Register
    ├── dashboard/             # Home tab (DashboardScreen/ViewModel)
    ├── explore/               # Explore tab (Request list; Helper tab placeholder)
    ├── request/               # RequestFormScreen/ViewModel (create+edit+delete), RequestCard
    └── profile/               # ProfileScreen (view + edit), ProfileViewModel
```

## Tech stack

- Kotlin 2.2.10, AGP 8.13.2, Gradle 8.13, JDK toolchain 21, compileSdk/targetSdk 34, minSdk 24
- Compose BOM 2024.04.01 (Material 3), Navigation Compose 2.8.5, Lifecycle 2.8.7
- Room 2.7.2 via KSP 2.2.10-2.0.2 (Room 2.6.x fails under KSP2 with "unexpected jvm signature V" — don't downgrade)
- DB uses `fallbackToDestructiveMigration` (pre-release). Add real migrations before shipping.

## Build & run

```
gradlew.bat :app:assembleDebug        # APK: app/build/outputs/apk/debug/app-debug.apk
```
Android SDK path is in `local.properties`. Emulator AVD `Pixel_8` exists; if it hangs "offline" after loading a snapshot, cold boot with `emulator -avd Pixel_8 -no-snapshot`.

## Status log

- 2026-10-07 — Started as static Login/Main Menu/Dashboard UI. Added Room, MVVM, Navigation Compose, Profile, and a generic Tasks CRUD.
- 2026-10-07 — Switched to the Figma design (user's screenshots). Tasks → **Requests**; Home/Explore/Profile restyled; Add Request Details form built from Figma. Scope limited to the rubric on purpose ("build sampai tahap rubrik dulu").
- 2026-10-07 — Verified on emulator: login validation + wrong password, Home stats, Explore list, form validation, create (Tomorrow → Urgent badge), edit (deadline change), delete with confirm, profile edit + save, logout with confirm, Back after logout stays on Login, session kept after reinstall.
- 2026-10-07 — Committed in 7 commits and pushed to https://github.com/dero-10/SOS (main).

## Next phase (from Figma, not built yet)

- [ ] Bottom nav: add **Chat** and **Recent** tabs (Figma order: Home, Explore, Chat, Recent, Profile).
- [ ] **Helpers**: `HelperEntity` (rating, helps, requests, reviews, online, skills, education) + Explore → Helper tab cards + Helper profile screen (stats, Recent Helps, Skills, "Chat Helper").
- [ ] **Chat**: messages table, chat list (search, time ago), chat detail; "Chat Helper" → Add Request Details → Send posts the request as a chat bubble ("Request has been sent.").
- [ ] **Request detail** screens (own: teal "Request" header; others: author + Replies (N) with portfolio links + "Post a reply…"), reply count on Explore cards.
- [ ] **Recent** tab (history of the user's requests/helps).
- [ ] Profile: Withdraw / Buy Credits (currently "coming soon" snackbars), LinkedIn link, Language and Display settings.
- [ ] Inter font; request image upload/preview.
- [x] Commit in small meaningful commits (rubric D6).
