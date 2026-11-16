# Panduan Class untuk Presentasi UTS

## Alur aplikasi

1. `Main.main()` memilih Look and Feel sistem dan menjadwalkan GUI melalui `SwingUtilities.invokeLater`.
2. `MainFrame` menjadi jendela tunggal yang memuat dua kondisi: layar autentikasi (`AuthScreen`) dan ruang aplikasi (`AppShell`).
3. `AuthScreen` memvalidasi formulir, memanggil `AuthService`, lalu handing over `UserSession` ke `AppShell`.
4. `AppShell` memiliki sidebar (`Sidebar`), bar atas (`TopBar`), dan area konten yang bertukar halaman lewat `CardLayout`.
5. Setiap halaman membaca dan menulis data melalui service sesi: `AuthService`, `ConnectionService`, atau `LearningWorkspaceService`.
6. Service menerapkan aturan akses peran dan kepemilikan, lalu memanggil kontrak `Repository<T>`.
7. `JsonRepository<T>` membaca/menulis berkas JSON pada direktori data pengguna secara atomik.

## Class utama

| Class | Peran |
|---|---|
| `Main` | Entry point aplikasi desktop. |
| `MainFrame` | Jendela utama, berpindah antara layar autentikasi dan ruang aplikasi. |
| `AuthScreen` | Formulir masuk dan registrasi dengan pesan galat inline. |
| `AppShell` | Rangka aplikasi: sidebar, pemilih siswa, bar atas, dan pemetaan halaman. |
| `Workspace` | Menyimpan service, akun yang masuk, dan siswa yang sedang dipilih; memberi tahu halaman saat cakupan berubah. |
| `LearningTablePage<T>` | Dasar semua halaman CRUD belajar: tabel, pencarian, aksi tambah/edit/hapus, dan validasi cakupan siswa. |
| `DataTable<T>` | Komponen tabel generik dengan pencarian, empty state, dan status pill. |
| `FormDialog` | Dialog formulir modal dengan validasi wajib dan pesan galat di dalam dialog. |
| `ui.design` | Token desain dan komponen reusable: `Theme`, `Ui`, `Sidebar`, `TopBar`, `PageHeader`, `StatCard`, `EmptyState`, `Dialogs`. |
| `UserAccount` | Identitas akun: nama, email, hash kata sandi, peran, dan status. |
| `TeacherInvitation` | Kode undangan berumur tujuh hari; hanya hash SHA-256 yang disimpan. |
| `TeacherConnectionRequest` | Permintaan koneksi siswa ke pengajar beserta statusnya. |
| `TeacherStudentConnection` | Relasi pengajar-siswa; hanya relasi `ACTIVE` yang memberi akses data. |
| `LearningAccessGrant` | Kepemilikan record belajar terhadap akun siswa dan pengajar. |
| `StudentProfile` | Adapter profil tunggal versi awal, dipertahankan untuk kompatibilitas data lama. |
| `Pengguna` | Abstract base untuk identitas, kontak, dan kontrak label peran. |
| `Siswa` | Pengguna dengan tanggal lahir, jenjang, dan preferensi belajar. |
| `Pengajar` | Pengguna dengan bidang keahlian dan kualifikasi. |
| `Subject` | Mata pelajaran yang menjadi induk materi, jadwal, tugas, dan evaluasi. |
| `LearningMaterial` | Referensi belajar yang terhubung lewat `subjectId`. |
| `StudySession` | Jadwal yang menghubungkan ID siswa, pengajar, dan mata pelajaran serta waktu/durasi. |
| `LearningItem` | Abstract class untuk data berjudul yang memiliki status belajar. |
| `LearningTask` | Tugas dengan mata pelajaran, tenggat, status selesai, dan status terlambat. |
| `LearningGoal` | Target belajar dengan tanggal sasaran dan status tercapai. |
| `Evaluation` | Nilai dan nilai maksimum; `getPercentage()` menghitung persentase. |
| `Repository<T>` | Abstraksi operasi baca/tulis data tanpa mengikat service ke JSON. |
| `JsonRepository<T>` | Implementasi repository menggunakan Jackson dan file JSON atomik. |
| `ApplicationRepositories` | Membuat repository per jenis data dan menentukan direktori penyimpanan. |
| `AuthService` | Registrasi, login, sesi, ubah profil, dan ubah kata sandi. |
| `ConnectionService` | Siklus hidup undangan, permintaan koneksi, persetujuan, dan pencabutan relasi. |
| `LearningWorkspaceService` | CRUD data belajar yang sadar peran, GRANT kepemilikan, dan laporan progres. |
| `LearningService` | Service lama untuk data arsip pra-akun (`students.json`, `teachers.json`, `profiles.json`). |

## Contoh konsep OOP

### Encapsulation

`Evaluation` menyimpan `score` dan `maximumScore` sebagai atribut `private`. UI membuat/mengubah nilai melalui konstruktor atau `setScores(score, maximumScore)`, yang menolak nilai negatif, nilai non-finite, maksimum nol, dan nilai di atas maksimum. `UserAccount` tidak punya field maupun getter kata sandi polos.

### Inheritance dan polymorphism

`Siswa` dan `Pengajar` mewarisi identitas/kontak dari abstract `Pengguna` dan meng-override `getRoleLabel()`. `LearningTask` dan `LearningGoal` berbagi identitas serta judul dari `LearningItem`, lalu meng-override `getStatusLabel()` untuk tabel.

### Abstraction

`LearningWorkspaceService` menerima `ApplicationRepositories`, yang mengekspos data melalui `Repository<T>`. Service menggunakan operasi abstrak seperti `findAll()` dan `save()` tanpa perlu mengetahui detail serialisasi Jackson. `DataTable<T>` menerima `Supplier<List<T>>` dan `Function<T, Object[]>` sehingga tabel tidak tahu asal datanya.

### Polymorphism pada halaman

Semua halaman mengimplementasikan `AppShell.Refreshable`, sehingga `AppShell` dapat memuat ulang isi halaman saat dinavigasi tanpa mengetahui tipe konkretnya.

## Catatan presentasi

- Tunjukkan `LearningWorkspaceService.list()` dan `save()` sebagai lapisan yang memeriksa peran, relasi `ACTIVE`, dan `LearningAccessGrant` sebelum data sampai ke tabel.
- Tunjukkan `ConnectionService` sebagai siklus hidup undangan: kode tetap aktif sampai permintaan disetujui, sehingga penolakan tidak membuang kode.
- Tunjukkan `JsonRepository.write()` untuk penulisan sementara dan atomic move.
- Tunjukkan `DataTable<T>` dan `FormDialog` sebagai reuse generik UI.
- Tunjukkan `ui.design` sebagai satu sumber token visual sehingga seluruh halaman konsisten.
- Data uji dibuat di folder user, sehingga contoh saat demo bisa dihapus manual hanya setelah aplikasi ditutup dan file sudah dicadangkan.

Status build/pengujian saat dokumen ini ditulis dicatat di `README.md`; jangan menyebut tes JUnit lulus sebelum `mvn clean test` benar-benar dijalankan.
