# 🕌 Aplikasi Absensi Jamaah 5 Waktu (Sajada)

Aplikasi Android modern untuk pencatatan dan manajemen absensi jamaah shalat 5 waktu (Subuh, Dzuhur, Ashar, Maghrib, Isya) yang dirancang dengan **Clean Architecture**, **Jetpack Compose**, dan antarmuka bergaya **One UI + Glassmorphism**. Proyek ini siap diintegrasikan dengan perangkat keras pemindai sidik jari berbasis Bluetooth (ESP32/BLE Fingerprint Scanner).

---

## ✨ Fitur Utama

1. **Desain Modern (One UI Style + Glassmorphism)**
   - Sudut melengkung besar (large rounded corners) khas Samsung One UI.
   - Efek kaca semi-transparan (`GlassCard`) dengan border tipis bercahaya.
   - Dukungan penuh untuk **Dark Mode** dan **Light Mode** secara otomatis.

2. **Manajemen Jamaah (Tambah & Hapus)**
   - Dialog modal interaktif untuk mengelola anggota jamaah.
   - Pendaftaran anggota baru dengan nama dan pemetaan *Fingerprint ID* Bluetooth opsional.
   - Penghapusan data jamaah dengan pembaruan reaktif.

3. **Absensi Dual-Mode & Integrasi Bluetooth**
   - **Mode Manual**: Form admin cepat untuk memilih Jamaah, Waktu Shalat, dan mencatat kehadiran.
   - **Mode Bluetooth Fingerprint**: Panel status koneksi real-time (Indikator Hijau/Merah) dengan simulator scan hardware untuk pengujian tanpa perangkat fisik.

4. **Rekapan Log Absensi Interaktif (Edit & Hapus)**
   - Daftar kehadiran real-time dengan indikator status otomatis (`TEPAT_WAKTU`, `TERLAMBAT`, `DI_LUAR_JADWAL`).
   - Fitur **Edit** untuk mengubah status atau waktu shalat pada log yang tercatat.
   - Fitur **Hapus** untuk menghapus record log kehadiran.

5. **Mesin Kalkulasi Jadwal & Evaluasi Waktu Shalat**
   - Menggunakan `AttendanceEvaluatorUseCase` untuk mengevaluasi timestamp masuk dibanding jadwal shalat harian berdasarkan jendela waktu aktif (-15 menit hingga +30 menit dari adzan).

---

## 🏛️ Arsitektur Proyek (Clean Architecture)

Proyek ini terstruktur secara modular untuk memisahkan logika bisnis, sumber data, dan antarmuka pengguna:

```text
app/src/main/kotlin/com/sajada/app/
├── domain/                    # Clean Architecture: Domain Layer
│   ├── model/                 # Data Models & Enums (Jamaah, LogAbsensi, WaktuShalat, dll)
│   ├── repository/            # Repository Interfaces (JamaahRepository, AttendanceRepository)
│   └── usecase/               # Business Logic & Use Cases (Evaluator, Manual, Fingerprint, AddJamaah)
├── data/                      # Clean Architecture: Data Layer
│   ├── bluetooth/             # Bluetooth Driver Abstraction & Mock Implementation
│   └── repository/            # In-Memory Repositories (StateFlow / Flow reactive)
└── presentation/              # Presentation Layer (Jetpack Compose UI)
    ├── ui/
    │   ├── components/        # Reusable UI Components (GlassCard, dll)
    │   ├── screens/           # Main Dashboard & Dialogs
    │   └── theme/             # Material 3 Theme & Glassmorphism Colors
    └── viewmodel/             # ViewModel (StateFlow management)
```

---

## 📥 Download & Instalasi Publik

Aplikasi ini dikonfigurasi dengan **GitHub Actions CI/CD** yang secara otomatis mem-build APK setiap kali ada pembaruan kode.

Anda atau siapa pun dapat mendownload APK terbaru secara langsung melalui tautan berikut:
👉 **[Download APK Terbaru (GitHub Releases)](https://github.com/khafidzamrdn/sajada/releases/latest)**

---

## 🚀 Panduan Pengembangan Lokal

1. **Clone Repository**:
   ```bash
   git clone https://github.com/khafidzamrdn/sajada.git
   cd sajada
   ```

2. **Build Debug APK (menggunakan Gradle Wrapper)**:
   ```bash
   ./gradlew assembleDebug
   ```

3. **Menjalankan Unit Test Logika Inti**:
   ```bash
   node test/run.js
   ```

---

## 🛠️ Tech Stack

- **Bahasa**: Kotlin (Android Native) & JavaScript (Core Logic Test Runner)
- **UI Framework**: Jetpack Compose & Material 3
- **State Management**: Kotlin Coroutines & StateFlow
- **Arsitektur**: Clean Architecture (Domain, Data, Presentation)
- **CI/CD**: GitHub Actions (Auto APK Build & Release)

---

## 📄 Lisensi
Proyek ini bersifat open-source di bawah lisensi MIT.
