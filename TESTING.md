# Rencana & Laporan Testing — Pertemuan 13

**Kelompok:** (isi nama kelompok)
**Nama aplikasi:** SOS — Student on Study

| Anggota (Nama / NIM) | Username GitHub | Tugas testing | Berkas test yang ditulis |
|----------------------|-----------------|---------------|--------------------------|
| (isi) | deroow | Refactor interface repository, Fake repository, unit test Login/Register/ChatList/HelperProfile, UI test, CI | `FakeAuthRepository`, `FakeChatRepository`, `FakeHelperRepository`, `LoginViewModelTest`, `RegisterViewModelTest`, `ChatListViewModelTest`, `HelperProfileViewModelTest`, `LoginScreenTest`, `ChatListScreenTest` |
| (isi) | (isi) | (isi: mis. test ViewModel lain atau Mapper/Validator) | (isi — minimal 1 commit test) |
| (isi) | (isi) | (isi) | (isi — minimal 1 commit test) |

**Link repository:** https://github.com/dero-10/SOS
**Jenis proyek:**
- [x] Room (data lokal)
- [ ] Retrofit / REST API
- [ ] Firebase (Auth / Firestore / Storage) — memakai Supabase (login, profil, request) dengan fallback Room offline

---

## A. Audit Testability (hasil `cek_proyek.sh`)

Hasil awal: ✔2 ⚠2 ✘14. Sesudah perbaikan: ✘0.

| No | Pemeriksaan | Sudah | Belum | Commit perbaikan |
|----|-------------|:-----:|:-----:|------------------|
| 1 | ViewModel menerima repository lewat constructor | ✔ | | `RecentViewModel`: waktu sekarang dibuat parameter `now` (tidak lagi `Calendar.getInstance()`) |
| 2 | Repository punya interface | ✔ (setelah refactor) | | "Extract Auth, Chat and Helper repository interfaces…" (`AuthRepository`/`AuthRepositoryImpl`, dst.) |
| 3 | ViewModel tidak memakai Context/Uri/Log/Toast/Firebase langsung | ✔ | | `android.util.Patterns` di validasi email diganti regex biasa agar jalan di JVM |
| 4 | Logika di fungsi murni | ✔ | | `validateEmail`, `validatePassword` |
| 5 | Screen yang diuji menerima ViewModel lewat parameter default | ✔ | | UI test memasang ViewModel berisi fake |
| 6 | Icon/IconButton punya `contentDescription` | ✔ | | 17 `null` sudah diperiksa: semuanya ikon dekoratif di samping teks |

Catatan: `RequestRepository`, `ReplyRepository` belum berinterface, jadi ViewModel yang memakainya (Explore, Dashboard, Recent, RequestForm/Detail) belum diuji.

## B. Daftar Unit yang Akan Diuji

| Jenis | Nama kelas / fungsi | Berkas | Prioritas (1–3) |
|-------|---------------------|--------|:---------------:|
| ViewModel | `LoginViewModel` | `ui/auth/AuthViewModels.kt` | 1 |
| ViewModel | `RegisterViewModel` | `ui/auth/AuthViewModels.kt` | 1 |
| ViewModel | `ChatListViewModel` | `ui/chat/ChatListViewModel.kt` | 2 |
| ViewModel | `HelperProfileViewModel` | `ui/helper/HelperProfileViewModel.kt` | 2 |
| Screen (UI) | `LoginScreen` | `ui/auth/LoginScreen.kt` | 1 |
| Screen (UI) | `ChatListScreen` | `ui/chat/ChatListScreen.kt` | 2 |

## C. Matriks Skenario ViewModel

| No | ViewModel | Skenario (Given / When / Then) | Jenis | Nama test |
|----|-----------|--------------------------------|-------|-----------|
| 1 | Login | Given akun valid / When submit / Then `loggedIn = true`, tanpa error | Sukses | `valid credentials log the user in` |
| 2 | Login | Given password salah / When submit / Then "Incorrect password" | Gagal | `wrong password shows an error and stays logged out` |
| 3 | ChatList | Given tidak ada percakapan / Then daftar kosong, `hasChats = false` | Kosong | `no conversations gives an empty list` |
| 4 | ChatList | Given 2 percakapan / When cari "fai" / Then hanya Faith | Aksi pengguna | `search filters by helper name but keeps hasChats` |
| 5 | Login | Given field kosong / When submit / Then error validasi, repository tidak dipanggil | Validasi | `empty fields are rejected without calling the repository` |

Total 15 test ViewModel (Login 5, Register 3, ChatList 4, HelperProfile 3) dan 4 UI test.

**Test double yang dipakai:**

| Dependency asli | Pengganti di test | Jenis | Alasan |
|-----------------|-------------------|-------|--------|
| `AuthRepositoryImpl` (Room + Supabase) | `FakeAuthRepository` | fake | Hasil login/register dapat diatur, tanpa jaringan |
| `ChatRepositoryImpl` (Room) | `FakeChatRepository` | fake | Daftar percakapan di memori |
| `HelperRepositoryImpl` (Room) | `FakeHelperRepository` | fake | Data helper di memori |

## D. Hasil

| Jenis test | Jumlah | Lulus | Perintah |
|------------|:------:|:-----:|----------|
| Unit test (JVM) | 15 | 15 | `./gradlew testDebugUnitTest` |
| UI test (Compose, emulator Pixel_8 API 35) | 4 | 4 | `./gradlew connectedDebugAndroidTest` |
| Integration test (DAO) | 0 | – | belum dibuat |

- Link run GitHub Actions yang hijau: https://github.com/dero-10/SOS/actions/runs/44334677469
- Bukti test bisa gagal: nilai harapan "Incorrect password" sempat diubah menjadi "MUTATED" → `LoginViewModelTest` merah (15 test, 1 gagal), lalu dikembalikan.

## E. Refleksi

1. **Bug yang ditemukan test:** belum ada bug logika yang ditemukan. Hambatan yang ketahuan saat menyiapkan test: `Patterns.EMAIL_ADDRESS` (Android) tidak tersedia di JVM test sehingga validasi email tidak bisa diuji; diganti regex biasa sebelum test dijalankan.
2. **Refactor:** repository diekstrak menjadi interface + `...Impl`, `AppContainer` memakai Impl. Tersulit: menjaga `AuthRepository` tetap dipakai ProfileViewModel dan lainnya tanpa mengubah perilaku.
3. **Test paling bernilai:** `wrong password shows an error…`, karena melindungi pesan error login dan memastikan pengguna tidak dianggap masuk.
4. **Belum diuji:** ViewModel yang bergantung pada `RequestRepository`/`ReplyRepository` (Explore, Dashboard, Recent, form request), DAO Room, dan alur Supabase online, karena belum berinterface / butuh server.
