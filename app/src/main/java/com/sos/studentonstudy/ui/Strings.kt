package com.sos.studentonstudy.ui

import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import com.sos.studentonstudy.data.AppLanguage
import androidx.compose.material3.Text as MaterialText

internal val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.English }

/**
 * Translates a UI string into the current language. The app's strings are English in code and
 * looked up here, so a string with no entry simply stays English.
 */
@Composable
@ReadOnlyComposable
internal fun tr(text: String): String = translate(text, LocalAppLanguage.current)

internal fun translate(text: String, language: AppLanguage): String {
    if (language == AppLanguage.English) return text
    INDONESIAN[text]?.let { return it }
    for ((pattern, replacement) in INDONESIAN_PATTERNS) {
        if (pattern.matches(text)) return pattern.replace(text, replacement)
    }
    return text
}

/**
 * Drop-in replacement for Material's `Text` that translates its string first.
 * Files import this instead of `androidx.compose.material3.Text`.
 */
@Composable
internal fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    style: TextStyle = LocalTextStyle.current
) {
    MaterialText(
        tr(text), modifier, color, fontSize, fontStyle, fontWeight, fontFamily, letterSpacing, textDecoration,
        textAlign, lineHeight, overflow, softWrap, maxLines, minLines, onTextLayout, style
    )
}

@Composable
internal fun Text(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current
) {
    MaterialText(
        text, modifier, color, fontSize, fontStyle, fontWeight, fontFamily, letterSpacing, textDecoration,
        textAlign, lineHeight, overflow, softWrap, maxLines, minLines, emptyMap(), onTextLayout, style
    )
}

private val INDONESIAN_PATTERNS = listOf(
    Regex("Replies \\((\\d+)\\)") to "Balasan ($1)",
    Regex("Due (.+)") to "Tenggat $1",
    Regex("(.+) added to your balance") to "$1 ditambahkan ke saldo Anda",
    Regex("(.+) withdrawn") to "$1 ditarik",
    Regex("You only have (.+)") to "Saldo Anda hanya $1",
    Regex("Budget is higher than your balance \\((.+)\\)") to "Budget melebihi saldo Anda ($1)",
    Regex("Password must be at least (\\d+) characters") to "Kata sandi minimal $1 karakter",
    Regex("Sent to (.+) · (.+)") to "Dikirim ke $1 · $2",
    Regex("You posted a request · (.+)") to "Anda memposting request · $1",
    Regex("Say hello to (.+)") to "Sapa $1",
    Regex("Remove (.+)") to "Hapus $1",
    Regex("(.+) ago") to "$1 lalu"
)

private val INDONESIAN = mapOf(
    // Navigation
    "Home" to "Beranda", "Explore" to "Jelajahi", "Chat" to "Obrolan", "Recent" to "Terbaru", "Profile" to "Profil",
    // Auth
    "Login" to "Masuk", "Register" to "Daftar", "Logout" to "Keluar", "Log out?" to "Keluar?",
    "Welcome back to Student on Study" to "Selamat datang kembali di Student on Study",
    "Create your Student on Study account" to "Buat akun Student on Study Anda",
    "Create account" to "Buat akun", "Already have an account?" to "Sudah punya akun?",
    "Don't have an account?" to "Belum punya akun?", "Password" to "Kata sandi",
    "Confirm password" to "Konfirmasi kata sandi", "Full name" to "Nama lengkap",
    "Enter your email" to "Masukkan email Anda", "Enter your password" to "Masukkan kata sandi Anda",
    "Enter your name" to "Masukkan nama Anda", "Show password" to "Tampilkan kata sandi",
    "Hide password" to "Sembunyikan kata sandi",
    "Demo account: demo@sos.com / password123" to "Akun demo: demo@sos.com / password123",
    "Email is required" to "Email wajib diisi", "Enter a valid email address" to "Masukkan alamat email yang valid",
    "Password is required" to "Kata sandi wajib diisi", "Passwords do not match" to "Kata sandi tidak cocok",
    "No account found for this email" to "Tidak ada akun dengan email ini", "Incorrect password" to "Kata sandi salah",
    "This email is already registered" to "Email ini sudah terdaftar", "Name is required" to "Nama wajib diisi",
    "You will need to log in again to access your requests." to "Anda perlu masuk lagi untuk mengakses request Anda.",
    // Home
    "Hello," to "Halo,", "Post a Request" to "Posting Request", "Your Balance" to "Saldo Anda",
    "Requests" to "Request", "Urgent" to "Mendesak", "This week" to "Minggu ini", "See all" to "Lihat semua",
    "Recent Requests" to "Request Terbaru", "No requests yet" to "Belum ada request",
    "Post your first request to get help." to "Posting request pertama Anda untuk mendapat bantuan.",
    // Explore
    "Search" to "Cari", "Sort" to "Urutkan",
    "Newest" to "Terbaru", "Nearest deadline" to "Tenggat terdekat", "Highest budget" to "Budget tertinggi",
    "Recommended" to "Direkomendasikan", "Top rated" to "Rating tertinggi", "Most helps" to "Bantuan terbanyak",
    "Clear search" to "Hapus pencarian", "No matching requests" to "Tidak ada request yang cocok",
    "No matching helpers" to "Tidak ada helper yang cocok", "Try a different search or filter." to "Coba pencarian atau filter lain.",
    "Try a different name." to "Coba nama lain.", "Nothing here yet" to "Belum ada apa-apa",
    "Tap + to post a new request." to "Ketuk + untuk memposting request baru.", "Add request" to "Tambah request",
    "Online today" to "Online hari ini", "Verified" to "Terverifikasi",
    // Helper profile
    "Skills" to "Keahlian", "Helps" to "Bantuan", "Reviews" to "Ulasan",
    "Recent Helps" to "Bantuan Terakhir", "No helps yet" to "Belum ada bantuan",
    "Helper not found" to "Helper tidak ditemukan", "This helper is no longer available." to "Helper ini sudah tidak tersedia.",
    // Request detail / form
    "Add Request Details" to "Tambah Detail Request", "Edit Request" to "Ubah Request", "Edit request" to "Ubah request",
    "Delete request" to "Hapus request", "Delete request?" to "Hapus request?", "Delete" to "Hapus",
    "Title" to "Judul", "Input title" to "Masukkan judul", "Request details" to "Detail request",
    "Input text" to "Masukkan teks", "+ Add Attachment" to "+ Tambah Lampiran", "Remove attachment" to "Hapus lampiran",
    "Request image" to "Gambar request", "Estimated time" to "Perkiraan waktu", "Choose estimated time" to "Pilih perkiraan waktu",
    "Fast (< 1 hour)" to "Cepat (< 1 jam)", "Moderate (< 1 day)" to "Sedang (< 1 hari)", "Extended (>1 day)" to "Lama (>1 hari)",
    "Deadline" to "Tenggat", "Today" to "Hari ini", "Tomorrow" to "Besok", "3 days" to "3 hari", "1 week" to "1 minggu",
    "Custom date" to "Tanggal lain", "Increase budget" to "Naikkan budget", "Decrease budget" to "Turunkan budget",
    "Tags" to "Tag", "Send" to "Kirim", "Save" to "Simpan", "Cancel" to "Batal", "Close" to "Tutup", "Back" to "Kembali",
    "Title is required" to "Judul wajib diisi", "Title must be at least 5 characters" to "Judul minimal 5 karakter",
    "Request details are required" to "Detail request wajib diisi",
    "Describe your request in at least 10 characters" to "Jelaskan request Anda minimal 10 karakter",
    "Choose a deadline" to "Pilih tenggat", "Pick at least one tag" to "Pilih minimal satu tag",
    "Post a reply…" to "Tulis balasan…", "No replies yet." to "Belum ada balasan.", "Attach file" to "Lampirkan file",
    "You replied to this request" to "Anda membalas request ini", "You sent your request" to "Anda mengirim request",
    "Request has been sent." to "Request telah dikirim.", "send to helper" to "kirim ke helper",
    // Chat
    "Message" to "Pesan", "No conversations yet" to "Belum ada percakapan", "No matching conversations" to "Tidak ada percakapan yang cocok",
    "Open a helper's profile and tap Chat Helper to start one." to "Buka profil helper dan ketuk Chat Helper untuk memulai.",
    "No messages yet" to "Belum ada pesan", "Sent an attachment" to "Mengirim lampiran", "Just now" to "Baru saja", "Now" to "Sekarang",
    // Recent
    "Earlier this week" to "Awal minggu ini", "Earlier" to "Sebelumnya", "Yesterday" to "Kemarin",
    "Requests you post, send or reply to will show up here." to "Request yang Anda posting, kirim, atau balas akan muncul di sini.",
    // Profile
    "Edit Profile" to "Ubah Profil", "Major" to "Jurusan", "University" to "Universitas",
    "e.g. Product Design" to "mis. Desain Produk",
    "linkedin.com/in/yourname" to "linkedin.com/in/namaanda",
    "Enter a LinkedIn link, e.g. linkedin.com/in/yourname" to "Masukkan tautan LinkedIn, mis. linkedin.com/in/namaanda",
    "Add your LinkedIn link in Edit Profile" to "Tambahkan tautan LinkedIn Anda di Ubah Profil", "Profile saved" to "Profil disimpan",
    "Withdraw" to "Tarik Saldo", "Buy Credits" to "Beli Kredit", "Buy" to "Beli", "Amount (IDR)" to "Jumlah (IDR)", "e.g. 50000" to "mis. 50000",
    "Enter an amount" to "Masukkan jumlah", "Balance changed, please try again" to "Saldo berubah, coba lagi",
    "Adds credits to your SOS balance. This is a demo, no real payment is made." to "Menambah kredit ke saldo SOS Anda. Ini demo, tidak ada pembayaran sungguhan.",
    "Moves money out of your SOS balance. This is a demo, nothing is sent to a bank." to "Memindahkan uang dari saldo SOS Anda. Ini demo, tidak ada yang dikirim ke bank.",
    "Language" to "Bahasa", "Display" to "Tampilan", "Light mode" to "Mode terang", "Dark mode" to "Mode gelap", "Selected" to "Dipilih"
)
