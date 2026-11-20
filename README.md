# Personal Homeschooling Learning System

Aplikasi desktop Java Swing untuk menghubungkan siswa dan pengajar homeschooling melalui akun lokal dan kode undangan. Seluruh antarmuka memakai material "liquid glass": kanvas gradien biru es dan lavender, permukaan transparan berlapis, tepi rambut bercahaya, bayangan lembut, dan aksen indigo yang terkendali.

## Menjalankan aplikasi

```powershell
mvn clean test
mvn exec:java
```

Maven Wrapper ikut disertakan, jadi tanpa instalasi Maven pun bisa memakai `./mvnw.cmd clean test` (Windows) atau `./mvnw clean test` (macOS/Linux). Jalankan perintah tersebut dengan JDK 21.

## Akun demo

Aplikasi membuat data contoh pada pemakaian pertama sehingga tidak ada layar yang kosong. Akun demo juga ditampilkan pada layar masuk:

| Peran | Nama | Email | Kata sandi |
| --- | --- | --- | --- |
| Pengajar | Rani Puspita | `rani@demo.test` | `DemoGuru2024!` |
| Pengajar | Bagus Wicaksono | `bagus@demo.test` | `DemoGuru2024!` |
| Pengajar | Siti Nurhaliza | `siti@demo.test` | `DemoGuru2024!` |
| Siswa | Dimas Ardi (SMA) | `dimas@demo.test` | `DemoSiswa2024!` |
| Siswa | Nadia Putri (SMP) | `nadia@demo.test` | `DemoSiswa2024!` |
| Siswa | Yusuf Maulana (SMP) | `yusuf@demo.test` | `DemoSiswa2024!` |
| Siswa | Aulia Rahma (SD) | `aulia@demo.test` | `DemoSiswa2024!` |

Pengajar Rani terhubung dengan Dimas, Nadia, dan Yusuf. Bagus terhubung dengan Aulia. Yusuf juga mengirim permintaan koneksi ke Siti dan Aulia mengirim permintaan ke Rani, sehingga halaman Permintaan siswa dan Undangan tidak kosong.

### Cara membedakan Data demo dari data pengguna

Metadata penanda demo disimpan dalam tabel SQLite `demo_accounts`, `demo_resources`,
`seeded_students`, dan `seeded_teachers`. Berkas lama `demo-data.json` digunakan
sebagai sumber satu kali saat migrasi dan tetap dipertahankan sebagai recovery copy.
Struktur lama yang sempat digunakan:

- `accounts`: daftar id akun yang dibuat oleh seeding demo.
- `resources`: daftar id record per jenis (`subject`, `material`, `task`, `session`, `evaluation`, `goal`) yang dibuat oleh seeding demo.
- `seededStudents` dan `seededTeachers`: akun yang kurikulum demo-nya sudah dibuat.

Antarmuka memakai penanda ini untuk menampilkan pill "Data demo" pada detail materi, tugas, dan evaluasi, pill "Akun demo" pada halaman profil, dan pil "Demo" pada daftar orang terhubung. Aturan seeding:

1. Akun demo hanya dibuat bila email tersebut belum terdaftar. Akun yang sudah ada tidak pernah diubah.
2. Kurikulum demo hanya dibuat untuk siswa yang belum memiliki materi, tugas, jadwal, evaluasi, atau target. Data siswa yang sudah ada tidak pernah ditimpa.
3. Penanda SQLite memastikan seeding tidak pernah dijalankan dua kali untuk siswa yang sama, sehingga data demo yang dihapus pengguna tidak muncul kembali.
4. Data pengguna yang sudah ada sebelum aplikasi ini dijalankan tetap utuh dan dapat dibaca seperti semula.

## Isi data demo

- 7 mata pelajaran per siswa: Matematika, Bahasa Indonesia, IPA, IPS, Bahasa Inggris, PJOK, dan Seni Budaya.
- 10 materi per siswa berisi tujuan pembelajaran, pembahasan, contoh soal, latihan, dan kunci jawaban (Matematika, Bahasa Indonesia, IPA, IPS, Bahasa Inggris, PJOK, Seni Budaya).
- 12 tugas per siswa dengan instruksi, tenggat, dan status yang berbeda (belum selesai, selesai, dan terlambat).
- 8 sesi jadwal pada hari dan jam yang berbeda.
- 7 evaluasi berisi nilai, tanggal, dan umpan balik pengajar.
- 4 target belajar beserta tanggal target dan status pencapaiannya.

## Status implementasi

- Login, registrasi akun siswa/pengajar, logout, validasi peran, sesi dalam memori, ubah profil, dan ubah kata sandi.
- Kebijakan prototipe mengizinkan registrasi mandiri pengajar; belum ada verifikasi identitas atau persetujuan administrator.
- Pengajar membuat kode undangan acak berumur tujuh hari, melihat status beserta jumlah permintaan yang menunggu, dan mencabut kode yang masih aktif. Kode hanya ditampilkan saat dibuat; file lokal menyimpan hash SHA-256 kode.
- Kode undangan tetap aktif sampai ada permintaan yang disetujui, sehingga penolakan tidak membuang kode dan siswa dapat mengajukan ulang.
- Siswa mengirim permintaan koneksi; pengajar menyetujui atau menolak. Relasi `ACTIVE` hanya dibuat oleh persetujuan pengajar dan dapat diakhiri kembali dari halaman Data siswa.
- Dashboard dan laporan progres dihitung dari repository, bukan dari daftar hard-coded. Ringkasan laporan ditulis dalam bahasa Indonesia dari angka yang benar-benar tersimpan, dan daftar "Aktivitas terbaru" disusun dari jadwal, tugas, evaluasi, dan target yang sama.
- Pengajar membuat, mengubah, dan menghapus record belajar untuk siswa yang memiliki relasi `ACTIVE`. Siswa hanya dapat melihat record yang ditautkan kepadanya dan menandai tugas miliknya selesai. Pemeriksaan dilakukan di `LearningWorkspaceService`.
- Record baru memiliki grant kepemilikan tersendiri dalam `learning-access-grants.json`. Record lama tanpa grant tidak ditampilkan dan tidak dapat diklaim lewat pemakaian ulang ID. Tidak ada migrasi otomatis atas data lama.
- Halaman belajar memakai satu komponen tabel generik (`DataTable<T>`) dan satu dialog formulir generik (`FormDialog`). Semua halaman memakai token visual yang sama dari paket `ui.design`.
- Data arsip sebelum sistem akun (`students.json`, `teachers.json`, `profiles.json`) tetap dibaca dan ditampilkan sebagai arsip pada halaman Data pengajar dan Data siswa.

## Modul

1. Login, registrasi, dan logout (`AuthScreen`, `AuthService`).
2. Dashboard dengan ringkasan, jadwal terdekat, tugas terdekat, dan aktivitas terbaru (`DashboardPage`).
3. Management data siswa: daftar siswa terhubung, ringkasan belajar, detail siswa, dan akhiri koneksi (`DirectoryPage`).
4. Informasi dan management data pengajar: daftar pengajar terhubung, materi yang dibagikan, dan detail pengajar (`DirectoryPage`).
5. Mata pelajaran (`SubjectPage`).
6. Jadwal belajar dengan nama hari dalam bahasa Indonesia (`SchedulePage`).
7. Materi beserta isi lengkap: tujuan pembelajaran, pembahasan, contoh, dan latihan (`MaterialPage`, `DetailDialog`).
8. Tugas dengan instruksi, tenggat, siswa target, dan status pengerjaan (`TaskPage`).
9. Evaluasi, nilai, dan umpan balik pengajar (`EvaluationPage`).
10. Laporan progres dengan ringkasan naratif dan rincian per mata pelajaran (`ProgressPage`).
11. Permintaan dan persetujuan koneksi guru-siswa (`TeacherRequestsPage`, `StudentConnectionsPage`, `InvitationsPage`).
12. Profil, pergantian kata sandi, dan navigasi sesuai peran (`ProfilePage`, `AppShell`).

## Desain antarmuka

Paket `ui.design` menjadi satu-satunya sumber gaya tampilan:

- `Theme` menyimpan seluruh token warna, tipografi, jarak, dan radius sudut.
- `Glass` menggambar permukaan glass: bayangan berlapis, badan transparan, tepi rambut, dan sorotan specular.
- `GlassPanel`, `GlassButton`, `GlassCanvas`, `GlassDialog`, dan `NavIcon` adalah komponen reusable yang dipakai bersama oleh seluruh layar.
- `DataTable`, `FormDialog`, `DetailDialog`, `EmptyState`, `PageHeader`, `StatCard`, `Sidebar`, dan `TopBar` memastikan tabel, formulir, dialog, dan navigasi terlihat sama di semua halaman.

Catatan implementasi: blur asli di balik jendela tidak dipakai karena Swing tidak menyediakan backdrop filter yang andal dan blur per frame akan memaksa seluruh jendela digambar ulang tiap gerakan mouse. Efek frosting dihasilkan dengan lapisan putih transparan, gradien halus, tepi bercahaya, dan bayangan lembut. Tidak ada animasi berjalan maupun thread render.

## Teknologi dan prasyarat

- JDK 21.
- Maven 3.9+ untuk build dan menjalankan project.
- NetBeans dengan dukungan project Maven.
- Jackson Databind dan Java Time untuk migrasi JSON; SQLite JDBC untuk persistence; JUnit 5 untuk test.

Hash kata sandi menggunakan PBKDF2-HMAC-SHA256 dari JDK, 210.000 iterasi, salt acak 16 byte, dan hasil 256 bit. Password tidak punya getter atau field plaintext. Password input pada layar dibersihkan setelah submit. Kode undangan menggunakan `SecureRandom` dan hanya hash SHA-256 yang disimpan.

## Membuka di NetBeans

1. Pilih **File > Open Project**.
2. Pilih folder project yang berisi `pom.xml`.
3. Tunggu NetBeans memuat project Maven dan dependensinya.
4. Atur Java Platform ke JDK 21 melalui **Project Properties > Build > Compile** bila diperlukan.
5. Jalankan project dengan `id.edu.homeschool.Main` sebagai main class.

NetBeans menyediakan Maven bawaan sehingga **Tools > Options > Java > Maven** sudah dapat dipakai untuk project ini.

## Penyimpanan dan migrasi

Direktori default: `<user.home>/.homeschooling-learning-system/data/`.

Database utama adalah `homeschooling.db`. Schema dibuat otomatis oleh `DatabaseManager`,
dan setiap repository model memakai prepared statement SQLite. Koneksi mengaktifkan
foreign keys, `busy_timeout`, dan WAL mode.

Saat database baru dibuat dan berkas JSON lama ditemukan, `JsonToSqliteMigrator`
membuat backup `json-backup-<timestamp>/`, membaca JSON lama, memindahkan record
beserta ID, status, timestamp, password hash, relasi, dan grant, lalu menandai
migrasi sebagai selesai pada tabel `app_migrations`. Migrasi adalah idempotent dan
berkas JSON asli tidak dihapus atau dimodifikasi.

File legacy seperti `students.json` tetap dipertahankan sebagai sumber recovery dan
arsip. Record lama tanpa learning access grant tetap tidak terlihat pada workspace
akun baru. Hapus atau restore database hanya setelah membuat backup database dan backup JSON.

SQLite dan JSON tidak dienkripsi, sesi hilang ketika aplikasi ditutup, dan tidak ada
server atau kontrol akses filesystem tambahan. Karena itu prototipe ini tidak
memberikan jaminan keamanan multi-pengguna setara layanan server.

## Struktur package

```text
src/main/java/id/edu/homeschool/
  Main.java
  model/       Entitas dan status domain
  repository/  Repository<T>, SQLite, migrasi JSON, dan database infrastructure
  service/     Autentikasi, sesi, koneksi, workspace, dan seeding demo
  ui/          Jendela, shell aplikasi, dan halaman
  ui/design/   Token desain dan komponen UI reusable
src/test/java/id/edu/homeschool/
  model/ repository/ service/ ui/ support/
tools/         Harness manual untuk membuat tangkapan layar
```

Harness tangkapan layar dijalankan manual di luar build Maven:

```powershell
javac -encoding UTF-8 -cp "target\classes;<jackson jars>" -d target\tools-classes tools\UiShowcase.java
java  -cp "target\classes;target\tools-classes;<jackson jars>" UiShowcase
```

Hasilnya 22 PNG di `target/screenshots/`.

## Konsep OOP

- **Encapsulation:** `UserAccount` menyimpan atribut privat dan memvalidasi email, role, status, serta format hash melalui setter. `Evaluation` menolak nilai di luar rentang maksimum.
- **Inheritance:** `Siswa` dan `Pengajar` mewarisi `Pengguna`; `LearningTask` dan `LearningGoal` mewarisi `LearningItem`.
- **Polymorphism:** model tugas dan target meng-override `getStatusLabel()`; tabel belajar menggunakan label status melalui accessor bersama. Semua halaman mengimplementasikan `AppShell.Refreshable` sehingga shell dapat memuat ulang isi halaman secara polimorfis.
- **Abstraction:** service akun, koneksi, dan workspace menerima `Repository<T>`; `JsonRepository<T>` menjadi implementasi penyimpanan. `DataTable<T>` menerima loader dan mapper sebagai fungsi sehingga tidak tahu asal datanya.
- **Pengecekan role:** `ConnectionService` dan `LearningWorkspaceService` memperoleh akun aktif dari token sesi yang disimpan dalam `SessionRegistry`, lalu memvalidasi peran, relasi `ACTIVE`, dan grant kepemilikan sebelum perubahan.

## Build dan pengujian yang dilakukan

- Build dan test dijalankan dengan Maven 3.9.16 (Maven Wrapper) dan JDK 21 (Temurin 21.0.12.1), command `mvnw.cmd clean test`.
- Hasil: **67 test lulus, 0 gagal, 0 error** pada 17 kelas test.
- Cakupan test:
  - `AuthFlowTest` — registrasi, login, logout, akun bertahan setelah restart, email ganda, kata sandi lemah, hash kata sandi, ubah profil, ubah kata sandi, akun nonaktif.
  - `ConnectionFlowTest` — undangan → permintaan → persetujuan, kode tetap aktif sampai disetujui, pengajuan ulang setelah penolakan, pengajar tidak dapat memproses permintaan milik pengajar lain, pembatasan peran, pencabutan relasi.
  - `LearningWorkspaceFlowTest` — pengajar membuat materi/tugas/jadwal/evaluasi dan siswa melihatnya, siswa tidak dapat membaca data siswa lain, pengajar tanpa koneksi ditolak, edit dan hapus memperbarui tabel, mata pelajaran terpakai tidak dapat dihapus, penandaan tugas oleh siswa, target diatur pengajar, data bertahan setelah restart, laporan progres sesuai repository, record lama tanpa grant tidak terlihat.
  - `LearningActivityTest` — aktivitas terbaru disusun dari record tersimpan, siswa target dibaca dari grant kepemilikan, siswa lain ditolak, ringkasan laporan bahasa Indonesia, ringkasan direktori mengikuti orang yang terhubung.
  - `DemoDataServiceTest` — akun dan koneksi demo terbentuk, kurikulum lengkap dengan relasi yang valid, materi berisi tujuan dan isi, tugas berisi instruksi dan diverse status, seeding tidak diulang dan data pengguna tidak ditimpa, record demo ditandai, siswa lain tidak dapat menjangkau data siswa pertama.
  - `AppShellSmokeTest` — semua menu terdaftar dan dapat dibuka, halaman khusus pengajar tidak muncul untuk siswa, tabel mengikuti repository, pencarian menyaring baris, direktori menampilkan orang terhubung, pergantian siswa terpilih mengganti isi tabel.
  - `LearningPagesTest` — halaman materi menampilkan record siswa, filter kolom mempersempit tabel dan dapat dikembalikan ke semua, siswa dan pengajar melihat tugas yang sama, siswa tidak membuka halaman khusus pengajar.
  - `AuthScreenTest` — formulir login benar-benar memanggil service dan menyerahkan sesi; kata sandi salah tidak membentuk sesi.
  - Test lama (`AuthServiceTest`, `ConnectionServiceTest`, `LearningServiceTest`, `LearningWorkspaceServiceTest`, `JsonRepositoryTest`, test model) tetap lulus tanpa perubahan.
- Aplikasi dijalankan sungguhan lewat `id.edu.homeschool.Main` (proses berjalan normal, direktori data terbentuk) dan `tools/UiShowcase.java`; 22 tangkapan layar aktual tersimpan di `target/screenshots/`. Harness dijalankan tiga kali berturut-turut dengan data baru dan selalu menghasilkan 22 tangkapan layar tanpa galat.

## Referensi penyimpanan password

Spring Security mendokumentasikan penyimpanan password satu arah serta encoder adaptif seperti BCrypt dan PBKDF2: [Password Storage](https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html).
