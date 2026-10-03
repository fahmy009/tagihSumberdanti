# 🌿 Tagih Sumberdanti - Aplikasi Manajemen Tagihan Internet & Otomatisasi MikroTik

**Tagih Sumberdanti** adalah aplikasi Android Native berbasis **Kotlin** dan **Jetpack Compose** yang dirancang khusus untuk manajemen penagihan internet ISP / RT-RW Net, sinkronisasi data pelanggan real-time dengan **Firebase Cloud Firestore**, serta otomatisasi buka isolir/isolir router via **MikroTik RouterOS Socket API (Port 8728 / IP Public / VPN)**.

---

## 👨‍💻 Developer
Developed with ❤️ by **Muhammad Fahmy**  
GitHub Repository: [https://github.com/fahmy009/tagihSumberdanti](https://github.com/fahmy009/tagihSumberdanti.git)

---

## ✨ Fitur Utama Aplikasi

### 👑 1. Fitur Admin & Dashboard Pro
- **Dashboard Real-Time**: Ringkasan Total Pendapatan, Jumlah Pelanggan Aktif/Nonaktif, dan Tagihan Belum Lunas.
- **📊 Traffic Monitor Ethernet MikroTik**: Monitoring kecepatan unduh (RX Mbps) & unggah (TX Mbps) router secara *real-time*.
- **📟 Router Status Card**: Indikator status koneksi router MikroTik (*Online / Offline*).
- **Buat Tagihan Massal**: Otomatisasi pembuatan tagihan awal bulan untuk seluruh pelanggan aktif (*Firestore Batch Write*).
- **Tagihan Satuan**: Fitur pembuatan tagihan khusus untuk 1 pelanggan pilihan.

### 📋 2. Kelola Pelanggan & Tagihan
- **Kelola Pelanggan**: Pendaftaran pelanggan baru dengan pilihan **Dropdown Paket Internet** dan **Mapping PPP Secret MikroTik**.
- **Fitur Filter Chips Tagihan**: Filter status *Semua, Belum Bayar, Lunas, Menunggak, Double, Double Hutang*.
- **Kirim Nota WhatsApp**: Mengirimkan rincian kuitansi tagihan ke WhatsApp pelanggan via Android Intent (konversi otomatis `08xx` -> `628xx`).

### 🛵 3. Layar Khusus Tukang Tagih (Penagih Lapangan)
- **Tab Real-Time Opsi A**:
  - `📋 Belum Tagih`: Menampilkan daftar rumah yang belum dilunasi.
  - `✅ Sudah Lunas`: Menampilkan riwayat pembayaran.
  - **Auto-Sync**: Begitu pembayaran diinput oleh salah satu penagih, data pelanggan **otomatis langsung berpindah** ke tab *Sudah Lunas* di semua HP penagih lain secara real-time.
- **Header Setoran Penagih**: Menghitung total uang setoran dan jumlah rumah yang ditagih khusus oleh penagih yang sedang login.
- **Proteksi Secret MikroTik**: String credential `pppSecret` asli **disembunyikan (masked)** dari penglihatan penagih untuk keamanan router.

### ⚡ 4. Otomatisasi Buka Isolir / Isolir MikroTik (Port 8728)
- **Native Binary Socket API Protocol (`java.net.Socket`)**: Terhubung langsung ke Port 8728 (atau port forwarding VPN) tanpa mengubah port ke 80/443.
- **Buka Isolir Otomatis (Lunas)**: Saat tombol *"Bayar Lunas"* atau *"Bayar Double"* ditekan, sistem secara otomatis:
  1. Mengubah status PPP Secret di MikroTik menjadi `disabled=no`.
  2. Memutus sesi aktif di `/ppp/active/remove` agar router Melakukan dial-up ulang instan. **Internet pelanggan otomatis menyala dalam 2 detik!**
- **Isolir Otomatis (Menunggak)**: Saat status diubah menjadi *"Menunggak"*, akun PPP Secret otomatis di-`disabled=yes` dan disesuaikan di router.

### ⚙️ 5. Menu Pengaturan 5 Sub-Tab
1. **`📅 Periode`**: Dropdown pilihan bulan (Januari-Desember) & tahun aktif.
2. **`🔌 MikroTik`**: Form pengisian IP Public/VPN, Port 8728, User, Password, dan tombol Tes Koneksi Router.
3. **`📶 Paket`**: CRUD Paket Internet & Harga (tersimpan di Firestore koleksi `paket`).
4. **`👤 Akun`**: CRUD Akun Login Admin & Tukang Tagih (tersimpan di Firestore koleksi `users`).
5. **`🔐 Biometrik`**: Sakelar On/Off Login Sidik Jari (Fingerprint Unlock).

### 🔑 6. Session Management & Validasi Ketat
- **Persistent Auto-Login (`SessionManager`)**: Pengguna tidak perlu mengetik PIN/Password berulang kali saat aplikasi ditutup & dibuka kembali.
- **Validasi Data Input**: Format nomor WhatsApp wajib angka valid (min. 10 digit), PIN minimal 4 digit angka, dan harga/nominal wajib positif (> Rp 0).

---

## 🛠️ Teknologi & Stack yang Digunakan

- **Language**: Kotlin Native
- **UI Framework**: Jetpack Compose & Material 3 (Clean White Theme + Fresh Leaf Green Accent `#16A34A`)
- **Database Backend**: Firebase Cloud Firestore (Real-Time Listener)
- **Router Protocol**: MikroTik RouterOS Native Binary Socket API (`java.net.Socket` Port 8728)
- **Session Persistence**: Android SharedPreferences (`SessionManager`)
- **Architecture**: MVVM (Model-View-ViewModel) + Kotlin Coroutines & StateFlow

---

## 🚀 Cara Menjalankan Proyek

1. **Clone Repository**:
   ```bash
   git clone https://github.com/fahmy009/tagihSumberdanti.git
   ```
2. **Buka di Android Studio**:
   Buka folder proyek di Android Studio (Iguana / Ladybug / versi terbaru).
3. **Firebase Setup**:
   Pastikan file `google-services.json` milik proyek Firebase Anda telah ditaruh di dalam folder `app/google-services.json`.
4. **Build & Run**:
   Pilih emulator atau koneksikan HP Android, lalu klik **Run ('app')** (`Shift + F10`).

---

© 2026 **Tagih Sumberdanti** - Developed by **Muhammad Fahmy**
