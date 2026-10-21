# SOS — Student on Study: Progress & Rubric Tracker

> Handoff file for humans and AI assistants. Read this first before changing the project.
> Last updated: 2026-10-22

## What the app is

SOS is a peer-help marketplace for students: a student posts a **help request** (details, attachment, estimated time, deadline, budget, tags), helpers reply or chat, and the student pays from an in-app **balance**. Every screen in the Figma export is now built; the only open items are listed under "Still open".

## Design source (Figma)

- **Figma file:** [SOS - Student on Study (Copy)](https://www.figma.com/design/l7TOames4VGmoCETYpcXab/SOS---Student-on-Study--Copy-?node-id=0-1&t=C9oYYLE8wrLuZpFf-1)
- File key: `l7TOames4VGmoCETYpcXab`, root node: `0:1`
- The Figma MCP server has **no access** to this file ("you don't have edit access"), so the design is taken from the exported PNG frames (`SOS - Student on Study (Copy).zip`, 9 frames, each named with its author: cathleen, Clift, Fahri). Frame → screen:

| Figma frame | Screen | Code |
|---|---|---|
| Chat helper landing request (cathleen) | Add Request Details (no title/tags when sent to a helper) | `ui/request/RequestFormScreen.kt` |
| Chat helper (cathleen) | Chat detail | `ui/chat/ChatDetailScreen.kt` |
| Chat page (cathleen) | Chat list | `ui/chat/ChatListScreen.kt` |
| Explore page (cathleen) | Explore → Request tab | `ui/explore/ExploreScreen.kt` |
| Explore page - helper (cathleen) | Explore → Helper tab | `ui/explore/ExploreScreen.kt`, `ui/helper/HelperComponents.kt` |
| Explore page - helper info (cathleen) | Helper profile | `ui/helper/HelperProfileScreen.kt` |
| Explore page - request info (cathleen) | Request detail (someone else's) | `ui/request/RequestDetailScreen.kt` |
| Request (Fahri) | Request detail (own, teal header) | `ui/request/RequestDetailScreen.kt` |
| Profile page (Clift) | Profile | `ui/profile/ProfileScreen.kt` |

- Bottom nav in Figma: Home, Explore, Chat, Recent, Profile (icons black, selected purple). There is **no Figma frame for Home or Recent**; Home is the rubric dashboard and Recent is our own interpretation (activity timeline).
- Design tokens are in [`ui/SosTheme.kt`](app/src/main/java/com/sos/studentonstudy/ui/SosTheme.kt): purple `#4F46E5`, background `#F4F4F4`, card `#F7F7F7` + shadow, teal chips `#D4E7E8`, field gray `#EEEDEB`, urgent red. The app uses **Inter** like the Figma (Regular/Medium/SemiBold/Bold in `res/font`, wired in `SosTheme.kt`; SIL OFL license in `third_party/inter/`). Text built on `BasicTextField` must set `fontFamily = Inter` itself.
- Deliberate deviations from the PNGs: the search bar at the top of the helper profile and request detail is left out (it would be a dead control), and the request detail shows the deadline ("Due …") because the form collects one.

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
- **D2 Dashboard (Home)** — `DashboardViewModel` combines `currentUser` + `myRequests` Flows: balance, Requests / Urgent / This week counts, 3 most recent requests. Updates live after any create/edit/delete.
- **D3 Navigation** — Navigation Compose. Tabs: `home`, `explore`, `chat`, `recent`, `profile`; screens: `login`, `register`, `request_form?requestId=&helperId=`, `request_detail/{id}`, `helper_profile/{id}`, `chat/{helperId}`. Bottom bar uses `popUpTo(HOME){saveState}` + `restoreState`; Back from a tab → Home → exit. Auth screens are popped after login; logout pops everything. ViewModels + `rememberSaveable` keep state on rotation.
- **D4 Profile** — Figma layout; name, LinkedIn badge (opens the saved link), email, major + university, balance from Room. "Edit Profile" saves name, major, university and a validated LinkedIn link via `AuthRepository.updateProfile`. Withdraw / Buy Credits open an amount dialog that updates the balance in Room (simulated, no payment backend; withdrawing more than the balance is rejected in the DAO query too).
- **D5 CRUD (Requests)** — Create: "Post a Request" (Home) or + FAB (Explore) → Add Request Details form. Read: Explore feed (everyone's public requests) with search, tag filter, sort, reply counts; Home recent list; request detail. Update: own request → detail → pencil → same form in edit mode. Delete: trash icon on the edit screen → confirm dialog. Validation: title ≥5 chars, details ≥10 chars, deadline required, ≥1 tag, budget ≤ balance. Attachment picks a real file; images are copied to app storage and previewed.
- **D6 Architecture** — `data/local` (Room) → `data/repository` → `ui/<feature>/<Feature>ViewModel` (StateFlow) → Composables. Manual DI via `SosApplication`/`AppContainer`; all ViewModels built in `ui/SosViewModelFactory.kt`.

## Design decisions worth knowing

- **Explore is a community feed.** It lists every public request (seeded students plus yours), so tapping a card opens the **detail**, not the edit form. Own requests get the teal "Request" header and a pencil that opens the edit form.
- **"Chat Helper" sends a private request.** Helper profile → Chat Helper → Add Request Details (no title/tags, as in Figma) → Send stores a request with `helperId` set (kept out of the Explore feed, shown in Recent and Home) and posts it as the first chat bubble ("Request has been sent."). The title is the first ~60 characters of the details. Private requests have no edit action.
- **Replies** are written as the logged-in user; helper replies are seeded. The paperclip on the reply bar and chat bar stores the picked file's name only.
- **Seed data** (`data/SeedData.kt`, runs only on an empty database): demo user, 9 helpers (Chris, Faith, John, Mary, User 2, Abby, William, Coco, Billy), 7 community requests with replies, 5 chat conversations, recent helps. Seeded students have unusable passwords.

## Project structure

```
app/src/main/java/com/sos/studentonstudy/
├── SosApplication.kt          # Application + AppContainer (DB, session, repositories)
├── MainActivity.kt
├── data/
│   ├── SessionManager.kt, SeedData.kt
│   ├── local/                 # Entities.kt (users, requests, helpers, help_history, replies, chat_messages), Daos.kt, SosDatabase.kt (v4)
│   └── repository/            # Auth, Request, Helper, Reply, Chat repositories
└── ui/
    ├── SosTheme.kt (colours + Inter typography), Format.kt, UiComponents.kt, Attachments.kt, SosViewModelFactory.kt
    ├── navigation/SosNavHost.kt
    ├── auth/                  # Login, Register
    ├── dashboard/             # Home tab
    ├── explore/               # Explore tab (Request + Helper tabs)
    ├── helper/                # Helper card, helper profile
    ├── request/               # Form (create/edit/delete/send-to-helper), detail, RequestCard, RequestImage
    ├── chat/                  # Chat list + detail
    ├── recent/                # Recent tab
    └── profile/               # Profile (view + edit + balance dialogs)
```

## Tech stack

- Kotlin 2.2.10, AGP 8.13.2, Gradle 8.13, JDK toolchain 21, compileSdk/targetSdk 34, minSdk 24
- Compose BOM 2024.04.01 (Material 3), Navigation Compose 2.8.5, Lifecycle 2.8.7
- Room 2.7.2 via KSP 2.2.10-2.0.2 (Room 2.6.x fails under KSP2 with "unexpected jvm signature V" — don't downgrade)
- DB uses `fallbackToDestructiveMigration`: **every schema bump (now v4) wipes local data and re-seeds**. Add real migrations before shipping. After a wipe the saved session may point at a user id that no longer exists; clear app data (or log out first) if the profile looks empty.

## Build & run

```
gradlew.bat :app:assembleDebug        # APK: app/build/outputs/apk/debug/app-debug.apk
```
Android SDK path is in `local.properties`. Emulator AVD `Pixel_8` exists; if it hangs "offline" after loading a snapshot, cold boot with `emulator -avd Pixel_8 -no-snapshot`. If the emulator refuses to start with "Insufficient RAM", add `-memory 1536`.

On this machine Gradle fails with `Unable to establish loopback connection` when started from a sandboxed/agent shell; run it from a normal terminal (or Android Studio).

## Status log

- 2026-10-07 — Started as static Login/Main Menu/Dashboard UI. Added Room, MVVM, Navigation Compose, Profile, and a generic Tasks CRUD.
- 2026-10-07 — Switched to the Figma design (user's screenshots). Tasks → **Requests**; Home/Explore/Profile restyled; Add Request Details form built from Figma. Scope limited to the rubric on purpose ("build sampai tahap rubrik dulu").
- 2026-10-07 — Verified on emulator: login validation + wrong password, Home stats, Explore list, form validation, create (Tomorrow → Urgent badge), edit (deadline change), delete with confirm, profile edit + save, logout with confirm, Back after logout stays on Login, session kept after reinstall.
- 2026-10-07 — Committed in 7 commits and pushed to https://github.com/dero-10/SOS (main).
- 2026-10-22 — Built the rest of the Figma export in 8 commits: data layer + seed, 5-tab bar, Helper tab + profile, request detail + replies, chat list/detail + Chat Helper flow, Recent, Profile polish (LinkedIn, balance dialogs), request images. Checked on the emulator: all tabs, helper profile, Chat Helper → Send → chat bubble, own/other request detail, posting a reply, Buy Credits. Not yet exercised on the emulator: image attachment picking, Withdraw, edit/delete after the detail-screen change, LinkedIn edit field, rotation on the new screens. Not pushed.
- 2026-10-22 — Added the Inter font (v4.1 static TTFs from the official rsms/inter release) and applied it in the theme; confirmed on the emulator.

## Still open

- [ ] Language and Display rows only list what exists today (English, Light mode); Bahasa Indonesia and Dark mode are shown as "Coming soon" (dark mode would need the colour constants in `SosTheme.kt` to become theme-aware).
- [ ] Deleted/replaced request images stay in app storage (`files/request_images`).
- [ ] Real Room migrations (see Tech stack).
