package id.edu.homeschool.service;

import id.edu.homeschool.model.AccountRole;
import id.edu.homeschool.model.DemoMarkers;
import id.edu.homeschool.model.Evaluation;
import id.edu.homeschool.model.LearningGoal;
import id.edu.homeschool.model.LearningMaterial;
import id.edu.homeschool.model.LearningTask;
import id.edu.homeschool.model.StudySession;
import id.edu.homeschool.model.Subject;
import id.edu.homeschool.repository.ApplicationRepositories;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Creates the sample curriculum that ships with the application so no screen is ever empty on
 * the first launch.
 *
 * <p>The seeder is deliberately conservative. It never overwrites an existing record: accounts
 * are matched by e-mail address, learning records are only created for a student that has no
 * learning data at all, and SQLite marker tables record what has already been seeded so the data is
 * never created twice. Deleting the demo records keeps them deleted, and user data is never
 * touched.</p>
 *
 * <p>The SQLite demo marker tables tell demo data apart from user data. The legacy
 * {@code demo-data.json} file is only a migration source and is preserved for recovery.</p>
 */
public final class DemoDataService {

    /** A demo account together with the password that can be used to sign in. */
    public record DemoAccount(String name, String email, String password, AccountRole role) { }

    private static final String TEACHER_PASSWORD = "DemoGuru2024!";
    private static final String STUDENT_PASSWORD = "DemoSiswa2024!";

    private static final List<DemoAccount> TEACHER_ACCOUNTS = List.of(
            new DemoAccount("Rani Puspita", "rani@demo.test", TEACHER_PASSWORD, AccountRole.PENGAJAR),
            new DemoAccount("Bagus Wicaksono", "bagus@demo.test", TEACHER_PASSWORD, AccountRole.PENGAJAR),
            new DemoAccount("Siti Nurhaliza", "siti@demo.test", TEACHER_PASSWORD, AccountRole.PENGAJAR));

    private static final List<DemoAccount> STUDENT_ACCOUNTS = List.of(
            new DemoAccount("Dimas Ardi", "dimas@demo.test", STUDENT_PASSWORD, AccountRole.SISWA),
            new DemoAccount("Nadia Putri", "nadia@demo.test", STUDENT_PASSWORD, AccountRole.SISWA),
            new DemoAccount("Yusuf Maulana", "yusuf@demo.test", STUDENT_PASSWORD, AccountRole.SISWA),
            new DemoAccount("Aulia Rahma", "aulia@demo.test", STUDENT_PASSWORD, AccountRole.SISWA));

    private final ApplicationRepositories repositories;
    private final AuthService auth;
    private final ConnectionService connections;
    private final LearningWorkspaceService learning;
    private DemoMarkers markers = new DemoMarkers();

    public DemoDataService(ApplicationRepositories repositories, AuthService auth, ConnectionService connections,
            LearningWorkspaceService learning) {
        this.repositories = Objects.requireNonNull(repositories);
        this.auth = Objects.requireNonNull(auth);
        this.connections = Objects.requireNonNull(connections);
        this.learning = Objects.requireNonNull(learning);
        loadMarkers();
    }

    /* ---------------- catalogue ---------------- */

    private record SubjectSeed(String name, String description) { }
    private record MaterialSeed(String subject, String title, String type, String location, String notes,
            String objectives, String content) { }
    private record TaskSeed(String subject, String title, String instructions, int dueOffset, boolean completed) { }
    private record SessionSeed(String subject, int dayOffset, LocalTime time, int duration, String notes) { }
    private record EvaluationSeed(String subject, int dayOffset, double score, double maximum, String notes) { }
    private record GoalSeed(String description, int targetOffset, boolean achieved) { }

    private static final List<SubjectSeed> SUBJECTS = List.of(
            new SubjectSeed("Matematika", "Aljabar, geometri, dan pengolahan data"),
            new SubjectSeed("Bahasa Indonesia", "Teks eksplanasi, puisi, dan penulisan argumentasi"),
            new SubjectSeed("IPA", "Sistem tubuh, ekosistem, dan besaran"),
            new SubjectSeed("IPS", "Perubahan sosial, ekonomi, dan kenegaraan"),
            new SubjectSeed("Bahasa Inggris", "Teks deskriptif, dialog, dan writing sederhana"),
            new SubjectSeed("PJOK", "Kebugaran, alimentasi, dan gaya hidup sehat"),
            new SubjectSeed("Seni Budaya", "Ragam seni visual dan musik Nusantara"));

    private static final List<MaterialSeed> MATERIALS = List.of(
            new MaterialSeed("Matematika", "Menyelesaikan Persamaan Linear Satu Variabel", "Materi baca",
                    "Bab 1 - Persamaan Linear", "Penjelasan, contoh soal, dan latihan bergaya",
                    """
                    Tujuan pembelajaran:
                    1. Mengenali bentuk umum persamaan linear satu variabel.
                    2. Menyelesaikan persamaan linear satu variabel dengan langkah yang benar.
                    3. Menerjemahkan soal cerita sederhana menjadi model persamaan.""",
                    """
                    Pembahasan

                    Persamaan linear satu variabel berbentuk ax + b = 0 dengan a dan b bilangan riil serta a tidak sama dengan nol. Huruf x menyatakan besaran yang dicari, a adalah koefisien, dan b adalah konstanta.

                    Langkah penyelesaian:
                    1. Kumpulkan seluruh suku yang memuat x ke satu ruas.
                    2. Pindahkan konstanta ke ruas yang berlawanan dengan mengubah tanda.
                    3. Bagi kedua ruas dengan koefisien x.

                    Contoh 1
                    Soal: 3x + 5 = 20
                    Penyelesaian: 3x = 20 - 5, maka 3x = 15, sehingga x = 5.
                    Pemeriksaan: 3 x 5 + 5 = 20, sehingga jawaban benar.

                    Contoh 2 (soal cerita)
                    Usia Adi 4 tahun lebih tua daripada Budi. Jumlah usia mereka 26 tahun. Tentukan usia masing-masing.
                    Misalkan usia Budi adalah x, maka usia Adi adalah x + 4.
                    x + (x + 4) = 26
                    2x = 22
                    x = 11
                    Budi 11 tahun dan Adi 15 tahun. Pemeriksaan: 11 + 15 = 26, sesuai.

                    Latihan
                    1. 7x - 9 = 26
                    2. 4(x + 3) = 32
                    3. (2x + 6) / 4 = x - 1
                    4. Sebuah bilangan dikurangi 8 menghasilkan 27. Tentukan bilangan tersebut.
                    5. Tentukan nilai x dari 5x + 3 = 3x + 11.

                    Kunci jawaban
                    1. x = 5
                    2. x = 5
                    3. x = 5
                    4. 35
                    5. x = 4"""),
            new MaterialSeed("Bahasa Indonesia", "Menyusun Teks Eksplanasi", "Materi baca",
                    "Bab 2 - Teks Eksplanasi", "Ciri teks, contoh, dan latihan menulis",
                    """
                    Tujuan pembelajaran:
                    1. Mengidentifikasi ciri teks eksplanasi.
                    2. Menentukan topik dan tujuan teks eksplanasi.
                    3. Menyusun teks eksplanasi dengan kalimat utama dan kalimat penjelas.""",
                    """
                    Pembahasan

                    Teks eksplanasi menjelaskan suatu fenomena atau peristiwa agar pembaca memahaminya. Teks ini biasanya muncul untuk menjawab pertanyaan mengapa atau bagaimana sesuatu terjadi.

                    Ciri teks eksplanasi:
                    1. Berisi informasi atau penjelasan tentang suatu fenomena.
                    2. Bersifat informatif dan tidak memancing emosi pembaca.
                    3. Menggunakan bahasa yang lugas, denotatif, dan runtut.
                    4. Susunannya: pernyataan umum, penjelasan, dan penutup.

                    Contoh teks eksplanasi

                    Judul: Proses Terjadinya Hujan
                    Hujan terjadi ketika uap air yang terdapat di udara mengembun dan berubah menjadi titik-titik air. Pendinginan udara membuat uap air naik ke lapisan atmosfer yang lebih dingin. Di lapisan tersebut uap air mengembun menjadi awan. Ketika butiran air dalam awan semakin berat, butiran itu jatuh ke permukaan bumi sebagai hujan. Siklus ini berlangsung terus-menerus dan disebut siklus air.

                    Latihan
                    1. Identifikasi kalimat utama beserta kalimat penjelas pada contoh teks di atas.
                    2. Susunlah teks eksplanasi 3 sampai 5 kalimat yang menjelaskan mengapa daun berwarna hijau.
                    3. Carilah satu teks eksplanasi dari buku atau internet, lalu tuliskan topiknya.

                    Kunci jawaban
                    1. Kalimat utama: Hujan terjadi ketika uap air yang terdapat di udara mengembun dan berubah menjadi titik-titik air. Kalimat penjelas: kalimat kedua sampai kalimat keempat.
                    2. Contoh jawaban: Daun berwarna hijau karena sel daun mengandung klorofil. Klorofil menyerap cahaya merah dan biru, tetapi memantulkan cahaya hijau. Cahaya hijau yang dipantulkan itulah yang sampai ke mata kita, sehingga daun terlihat hijau.
                    3. Topik bergantung pada teks yang dipilih, misalnya proses pasteurisasi susu."""),
            new MaterialSeed("IPA", "Sistem Pernapasan Manusia", "Materi baca",
                    "Bab 3 - Sistem Pernapasan", "Diagram organ, mekanisme napas, dan latihan",
                    """
                    Tujuan pembelajaran:
                    1. Menyebutkan organ-organ sistem pernapasan manusia beserta fungsinya.
                    2. Menjelaskan mekanisme inspirasi dan ekspirasi.
                    3. Menjelaskan pertukaran oksigen dan karbon dioksida di paru-paru.""",
                    """
                    Pembahasan

                    Sistem pernapasan manusia terdiri dari rongga hidung, faring, laring, trakea, bronkus, bronkiolus, alveolus, dan pleura. Pleura adalah selaput tipis yang membungkus paru-paru.

                    Organ dan fungsinya
                    1. Rongga hidung: pintu masuk udara sekaligus tempat udara disaring dan dihangatkan.
                    2. Faring: saluran menuju laring dan bagian belakang rongga mulut.
                    3. Laring: pintu masuk trakea yang dilindungi epiglotis.
                    4. Trakea: saluran udara bertulang rawan yang menghubungkan laring dengan bronkus.
                    5. Bronkus: cabang trakea menuju paru-paru kanan dan paru-paru kiri.
                    6. Bronkiolus: cabang bronkus yang semakin kecil menuju alveolus.
                    7. Alveolus: kantung udara tempat pertukaran gas berlangsung.

                    Mekanisme pernapasan
                    Inspirasi: diafragma berkontraksi dan melengkung ke bawah, volume rongga dada membesar, tekanan udara di paru-paru menurun, sehingga udara masuk.
                    Ekspirasi: diafragma kembali kendur, volume rongga dada mengecil, tekanan udara meningkat, sehingga udara keluar.

                    Pertukaran gas di alveolus
                    Oksigen dari alveolus berdifusi ke dalam darah dan diikat oleh hemoglobin. Karbon dioksida dari darah masuk ke alveolus lalu dikeluarkan saat ekspirasi.

                    Contoh soal
                    Seorang siswa menghitung napasnya selama 2 menit dan tercatat 36 kali. Berapa kali napas per menitnya?
                    Jawab: 36 dibagi 2 sama dengan 18 kali per menit.

                    Latihan
                    1. Sebutkan organ yang menghubungkan laring dengan bronkus.
                    2. Jelaskan mengapa diafragma melengkung ke bawah saat inspirasi.
                    3. Mengapa alveolus dikelilingi kapiler darah yang sangat rapat?
                    4. Hitung laju napas per menit jika dalam 3 menit tercatat 54 kali napas.

                    Kunci jawaban
                    1. Trakea.
                    2. Karena volume rongga dada membesar sehingga tekanan udara menurun dan udara masuk ke paru-paru.
                    3. Agar pertukaran oksigen dan karbon dioksida berlangsung cepat dan maksimal.
                    4. 54 dibagi 3 sama dengan 18 kali per menit."""),
            new MaterialSeed("IPS", "Perubahan Sosial dan Dampaknya", "Materi baca",
                    "Bab 4 - Perubahan Sosial", "Bentuk, dampak, dan sikap menghadapi perubahan",
                    """
                    Tujuan pembelajaran:
                    1. Mengidentifikasi bentuk-bentuk perubahan sosial.
                    2. Menganalisis dampak perubahan sosial terhadap kehidupan masyarakat.
                    3. Menentukan sikap yang tepat saat menghadapi perubahan sosial.""",
                    """
                    Pembahasan

                    Perubahan sosial adalah perubahan pada berbagai aspek kehidupan masyarakat, seperti budaya, ekonomi, pendidikan, teknologi, dan lingkungan. Perubahan ini dapat berlangsung cepat maupun lambat.

                    Bentuk perubahan sosial
                    1. Perubahan budaya: pergeseran tradisi, adat, dan nilai kehidupan masyarakat.
                    2. Perubahan ekonomi: perubahan cara masyarakat mencari nafkah, misalnya berkembangnya perdagangan daring.
                    3. Perubahan pendidikan: pembaruan kurikulum, metode belajar, dan fasilitas sekolah.
                    4. Perubahan teknologi: berkembangnya teknologi komunikasi yang mengubah cara belajar dan berkomunikasi.
                    5. Perubahan lingkungan: perubahan penggunaan lahan yang memengaruhi ketersediaan air dan ruang hijau.

                    Dampak perubahan sosial
                    Dampak positif: akses pendidikan dan informasi semakin luas, efisiensi kerja meningkat, dan lapangan usaha baru terbuka.
                    Dampak negatif: kesenjangan digital meningkat, ekspresi budaya lokal melemah, dan ketergantungan pada gawai bertambah.

                    Contoh kasus
                    Di sebuah kota kecil, berdirinya pusat perbelanjaan modern memberi dampak ganda. Pedagang kaki lima kehilangan pelanggan karena pembeli beralih ke ruko. Di sisi lain, warga mendapat akses barang yang lebih lengkap dan beragam, serta cara pembayaran nontunai yang lebih cepat. Remaja di daerah tersebut mulai mengenal penggunaan aplikasi belanja daring.

                    Sikap menghadapi perubahan
                    1. Sikap terbuka: menerima perubahan dan menilainya secara kritis.
                    2. Sikap selektif: mengambil bagian yang bermanfaat dan menolak bagian yang merugikan.
                    3. Sikap konservatif: mempertahankan kondisi lama. Sikap ini perlu dihindari karena dapat menghambat perkembangan.

                    Latihan
                    1. Daftar tiga dampak positif dan tiga dampak negatif perubahan teknologi pada kehidupan sekolah.
                    2. Jelaskan satu contoh perubahan sosial yang terjadi di lingkungan tempat tinggalmu.
                    3. Tentukan sikap yang tepat bagi siswa ketika ada program sekolah baru yang belum pernah dikenal.

                    Kunci jawaban
                    1. Dampak positif: pembelajaran daring, akses informasi, dan komunikasi lebih cepat. Dampak negatif: berkurangnya interaksi langsung, gangguan konsentrasi, dan berkurangnya kemampuan berbicara langsung.
                    2. Contoh jawaban: Di lingkungan desa, penerangan jalan menjadi lebih terang sehingga warga lebih mudah belajar pada malam hari. Sebaliknya, kebiasaan membaca anak menurun karena banyak waktu dihabiskan untuk bermain gawai.
                    3. Sikap terbuka, karena program baru dapat membawa manfaat bagi belajar siswa."""),
            new MaterialSeed("Bahasa Inggris", "Descriptive Text: My Home", "Materi baca",
                    "Unit 1 - Descriptive Text", "Contoh teks, kosakata, dan latihan menulis",
                    """
                    Tujuan pembelajaran:
                    1. Memahami struktur teks deskriptif.
                    2. Menggunakan kata ganti possessives dan preposisi untuk mendeskripsikan tempat tinggal.
                    3. Menyusun paragraf deskriptif sederhana tentang rumah.""",
                    """
                    Pembahasan

                    Teks deskriptif digunakan untuk menggambarkan objek, tempat, atau orang secara detail. Teks ini biasanya bangun dengan tiga bagian:

                    1. Paragraf pembuka yang menyatakan objek yang digambarkan.
                    2. Paragraf penjelasan yang menguraikan bagian atau ciri objek.
                    3. Paragraf penutup yang berisi kesimpulan atau kesan penulis.

                    Kosakata penting
                    living room = ruang tamu, study = ruang belajar, bedroom = kamar tidur,
                    garden = taman, two-storey = dua lantai, quiet = tenang, crowded = ramai.

                    Contoh teks

                    My Dream House
                    I have a dream house. It is a two-storey house in a quiet village near a rice field. My house has four rooms: a living room, a kitchen, two bedrooms, and a study. The walls are painted in soft blue and white. In the living room, there is a brown sofa and a big television. Behind the house, there is a small garden with orange trees and a wooden bench. My mother always says that the garden is the best part of the house because it gets fresh air in the morning. I really love my dream house because it is close to nature and far from the crowded city.

                    Latihan
                    1. Identify social paragraph, elaboration paragraph, dan closing paragraph pada contoh teks.
                    2. Jawab pertanyaan berikut dalam bahasa Inggris:
                       a. How many rooms does the dream house have?
                       b. What is in the living room?
                       c. Where is the garden?
                    3. Tuliskan teks deskriptif 5 sampai 7 kalimat tentang rumahmu sendiri.

                    Kunci jawaban
                    1. Social paragraph: kalimat pertama. Elaboration paragraph: kalimat kedua sampai kalimat kelima. Closing paragraph: dua kalimat terakhir.
                    2. a. It has four rooms. b. There is a brown sofa and a big television. c. The garden is behind the house.
                    3. Contoh jawaban: I live in a small house in Bandung. My house has two bedrooms and a kitchen. The front yard has a mango tree..."""),
            new MaterialSeed("PJOK", "Latihan Kebugaran untuk Remaja", "Materi baca",
                    "Bab 2 - Fitness Remaja", "Rumus denyut nadi, program latihan, dan prinsip latihan",
                    """
                    Tujuan pembelajaran:
                    1. Menghitung denyut nadi target dengan rumus denyut nadi maksimum.
                    2. Menyusun program latihan aerobik selama satu minggu.
                    3. Menerapkan prinsip latihan yang aman dan bertahap.""",
                    """
                    Pembahasan

                    Kebugaran jasmani terdiri atas komponen aerobik, kekuatan, kelenturan, dan keseimbangan. Untuk siswa SMA, latihan aerobik 3 sampai 5 kali seminggu dengan durasi 30 sampai 60 menit per sesi sudah memadai.

                    Rumus denyut nadi
                    Denyut nadi maksimum = 220 - usia
                    Denyut nadi target = 60% sampai 70% dari denyut nadi maksimum

                    Contoh perhitungan
                    Siswa berusia 15 tahun. Denyut nadi maksimum = 220 - 15 = 205 kali per menit. Rentang denyut nadi target = 205 x 0,6 = 123 sampai 205 x 0,7 = 143,5, sehingga rentang targetnya 123 sampai 143 kali per menit.

                    Contoh program mingguan
                    Senin: jalan cepat (brisk walking) 30 menit.
                    Selasa: latihan kekuatan ringan dengan beban tubuh, seperti sit-up, plank, dan push-up.
                    Rabu: bersepeda 45 menit.
                    Kamis: latihan kelenturan dan pernapasan 20 menit.
                    Jumat: bola voli bersama teman 60 menit.
                    Sabtu: hiking ringan 90 menit.
                    Minggu: istirahat aktif berupa jalan santai 20 menit.

                    Prinsip latihan
                    1. Peningkatan bertahap: volume latihan dinaikkan perlahan.
                    2. Istirahat cukup: beristirahat satu hari setelah dua hari latihan berat.
                    3. Hidrasi: minum air putih minimal delapan gelas sehari.
                    4. Alat yang aman: gunakan perlengkapan yang sesuai dan dalam kondisi baik.
                    5. Konsistensi lebih penting daripada intensitas tinggi.

                    Latihan
                    1. Seorang siswa berusia 17 tahun. Hitung denyut nadi maksimum dan rentang denyut nadi target saat latihan aerobik.
                    2. Susunlah program latihan aerobik selama lima hari.
                    3. Jelaskan mengapa beban latihan tidak boleh langsung dinaikkan secara drastis.
                    4. Hitung frekuensi denyut nadi per menit jika denyut nadi pada 10 detik pertama terukur 22 kali.

                    Kunci jawaban
                    1. Maksimum 203 kali per menit, rentang target 122 sampai 142 kali per menit.
                    2. Contoh: Senin brisk walking 30 menit, Selasa latihan kekuatan, Rabu bersepeda, Kamis kelenturan, Jumat bola voli.
                    3. Agar otot dan sistem kardiovaskular menyesuaikan secara bertahap sehingga risiko cedera dapat dihindari.
                    4. 22 x 6 sama dengan 132 denyut nadi per menit."""),
            new MaterialSeed("Seni Budaya", "Menggambar Motif Ragam Indonesia", "Materi baca",
                    "Bab 1 - Seni Ragam Nusantara", "Motif, simbol, prinsip menggambar, dan latihan",
                    """
                    Tujuan pembelajaran:
                    1. Mengidentifikasi ciri khas motif batik, ukiran, keramik, dan songket.
                    2. Memahami makna simbol yang terkandung dalam motif Nusantara.
                    3. Menerapkan unsur gambar dekoratif dalam karya sendiri.""",
                    """
                    Pembahasan

                    Motif Indonesia tidak pernah tanpa makna. Motif biasanya ditulis sebagai simbol yang dibaca oleh masyarakat dari generasi ke generasi.

                    Motif dan maknanya
                    1. Motif batik: kakak pinang, kapal, dan merak. Motif kapal bermakna bahwa kehidupan perlu dilalui dengan percobaan dan ketangguhan.
                    2. Motif ukiran kayu: tumbuhan, hewan, dan bentuk geometris. Motif tumbuhan melambangkan kehidupan, sedangkan bentuk geometris melambangkan kerapian.
                    3. Motif keramik: flora dan fauna seperti laba-laba dan burung yang melambangkan kelangsungan hidup.
                    4. Motif songket: bentuk bunga dan burung yang bermakna keagungan dan kemuliaan.

                    Prinsip menggambar motif
                    1. Bentuk dasar: mulailah dari bentuk sederhana seperti lingkaran, segitiga, dan persegi yang kemudian dimodifikasi.
                    2. Pengulangan: ulangi motif yang sama dengan variasi agar menghasilkan pola yang rapi.
                    3. Keseimbangan: tata unsur gambar agar terlihat seimbang dan tidak berat sebelah.
                    4. Ritme: pengulangan unsur menghasilkan ritme yang enak dilihat.

                    Langkah membuat karya
                    1. Tentukan tema, misalnya flora atau fauna Nusantara.
                    2. Buat beberapa bentuk dasar pada kertas.
                    3. Kembangkan menjadi motif dengan pengulangan yang rapi.
                    4. Beri warna sesuai karakter dan tema karya.
                    5. Periksa keseimbangan dan garis kontur.

                    Latihan
                    1. Jelaskan tiga perbedaan motif batik dan motif ukiran kayu.
                    2. Tuliskan makna motif kapal dalam batik.
                    3. Buat satu motif sederhana berbentuk flora dalam kotak 5 x 5 kisi.
                    4. Jelaskan mengapa motif yang dibuat perlu warna yang sesuai.

                    Kunci jawaban
                    1. Batik umumnya dibatik dengan canting dan tinta, sedangkan ukiran kayu menggunakan pahat.

                    Catatan: kunci jawaban soal 1 dan 2 menjadi bahan diskusi bersama pengajar.
                    2. Motif kapal bermakna bahwa kehidupan perlu dilalui dengan percobaan dan ketangguhan.
                    3. Gambar motif sederhana pada kertas 10 x 10 cm.
                    4. Jawabannya menjadi bahan diskusi: motif yang dibuat perlu memiliki pola yang jelas dan warna yang sesuai."""),
            new MaterialSeed("Matematika", "Himpunan dan Operasinya", "Materi baca",
                    "Bab 2 - Himpunan", "Notasi himpunan, operasi himpunan, dan soal cerita",
                    """
                    Tujuan pembelajaran:
                    1. Menyebutkan cara menulis himpunan dengan daftar dan dengan sifat.
                    2. Menghitung operasi himpunan: gabungan, irisan, komplemen, dan selisih.
                    3. Menyelesaikan soal cerita dengan konsep himpunan.""",
                    """
                    Pembahasan

                    Himpunan adalah kumpulan objek yang dapat dibedakan satu sama lain. Himpunan dilambangkan dengan huruf besar dan ditulis di dalam kurung siku.

                    Cara menulis himpunan
                    1. Dengan daftar: A = {1, 2, 3, 4}
                    2. Dengan sifat: B = {x | x < 5 dan x bilangan prima} = {2, 3}

                    Operasi himpunan
                    Gabungan (A u B): seluruh anggota A atau anggota B.
                    Irisan (A n B): anggota yang ada di A sekaligus ada di B.
                    Komplemen (A'): anggota semesta yang tidak ada di A.
                    Selisih (A - B): anggota A yang tidak ada di B.

                    Contoh
                    A = {1, 2, 3, 4, 5} dan B = {3, 4, 5, 6, 7}
                    A u B = {1, 2, 3, 4, 5, 6, 7}
                    A n B = {3, 4, 5}
                    A' dalam semesta {1, 2, 3, 4, 5, 6, 7} = {6, 7}
                    A - B = {1, 2}

                    Contoh soal cerita
                    Dari 30 siswa, 18 mengikuti ekstrakurikuler olahraga, 15 mengikuti Pramuka, dan 7 mengikuti keduanya. Berapa siswa yang mengikuti setidaknya salah satu kegiatan?
                    Misalkan himpunan siswa olahraga adalah O dan himpunan siswa Pramuka adalah P.
                    |O u P| = |O| + |P| - |O n P| = 18 + 15 - 7 = 26 siswa.
                    Jadi siswa yang tidak mengikuti keduanya adalah 30 - 26 = 4 orang.

                    Latihan
                    1. Tentukan A u B, A n B, dan A - B untuk A = {2, 4, 6, 8} dan B = {1, 2, 3, 4}.
                    2. Dari 40 siswa, 22 mengikuti kegiatan membaca, 18 mengikuti atletik, dan 8 melakukan keduanya. Berapa siswa yang tidak mengikuti keduanya?
                    3. Tuliskan himpunan bilangan prima yang kurang dari 10 dengan notasi pembentuk.
                    4. Jika A' = {1, 3, 5} dan semesta {1, 2, 3, 4, 5, 6}, tentukan himpunan A.

                    Kunci jawaban
                    1. A u B = {1, 2, 3, 4, 6, 8}; A n B = {2, 4}; A - B = {6, 8}.
                    2. 22 + 18 - 8 = 32 siswa, jadi 40 - 32 = 8 siswa.
                    3. {x | x bilangan prima dan x < 10} = {2, 3, 5, 7}.
                    4. A = {2, 4, 6}."""),
            new MaterialSeed("Bahasa Indonesia", "Membangun Paragraf Argumentasi", "Materi baca",
                    "Bab 3 - Paragraf Argumentasi", "Struktur paragraf, contoh, dan latihan",
                    """
                    Tujuan pembelajaran:
                    1. Menyebutkan struktur paragraf argumentasi.
                    2. Menyusun alasan yang logis dan relevan untuk mendukung pendapat.
                    3. Menulis paragraf argumentasi dengan kesimpulan yang jelas.""",
                    """
                    Pembahasan

                    Paragraf argumentasi menjelaskan pendapat dengan memberi alasan dan bukti yang logis. Berbeda dengan deskripsi, paragraf argumentasi selalu memiliki pendapat yang jelas.

                    Struktur paragraf argumentasi
                    1. Kalimat pendapat atau tesis di awal paragraf.
                    2. Kalimat alasan dan bukti yang mendukung pendapat.
                    3. Kalimat penjelas atau contoh yang memperkuat alasan.
                    4. Kalimat kesimpulan yang menegaskan kembali pendapat.

                    Contoh paragraf
                    Membaca secara rutin memberi dampak positif bagi hasil belajar siswa. Penelitian menunjukkan bahwa siswa yang membaca 30 menit setiap hari memiliki daya Ingat yang lebih baik dibandingkan siswa yang jarang membaca. Selain itu, kebiasaan membaca melatih konsentrasi dan memperluas kosakata. Oleh karena itu, guru dan orang tua perlu mendorong anak untuk membentuk kebiasaan membaca sejak dini.

                    Latihan
                    1. Tentukan kalimat pendapat, kalimat alasan, dan kalimat kesimpulan pada contoh paragraf di atas.
                    2. Susunlah satu paragraf argumentasi tentang pentingnya belajar di rumah dengan minimal empat kalimat.
                    3. Berikan satu alasan dan satu bukti yang relevan untuk pendapat bahwa membaca buku setiap hari bermanfaat.

                    Kunci jawaban
                    1. Pendapat: Membaca secara rutin memberi dampak positif bagi hasil belajar siswa. Alasan: penelitian menunjukkan...(kalimat kedua). Kesimpulan: Oleh karena itu, guru dan orang tua perlu mendorong anak untuk membentuk kebiasaan membaca sejak dini.
                    2. Contoh jawaban: Belajar di rumah memberikan waktu yang lebih fleksibel bagi siswa. Siswa dapat mengatur materi sendiri dan berkonsentrasi tanpa gangguan. Latihan soal yang dilakukan secara rutin membuat pemahaman materi lebih baik. Karena itu, belajar di rumah tetap dapat mencapai hasil yang baik asal dilakukan dengan disiplin.

                    Catatan: jawaban nomor 2 dan 3 dapat bervariasi dan dibahas bersama pengajar."""),
            new MaterialSeed("IPA", "Ekosistem dan Rantai Makanan", "Materi baca",
                    "Bab 4 - Ekosistem", "Produsen, konsumen, dekomposer, dan aliran energi",
                    """
                    Tujuan pembelajaran:
                    1. Menyebutkan komponen penyusun ekosistem.
                    2. Menyusun rantai makanan dan jaring makanan.
                    3. Menjelaskan aliran energi pada suatu ekosistem.""",
                    """
                    Pembahasan

                    Ekosistem adalah kesatuan antara komunitas makhluk hidup dengan lingkungan tidak hidup yang saling berinteraksi. Komponen utamanya meliputi produsen, konsumen, dan dekomposer.

                    Komponen ekosistem
                    1. Produsen: makhluk hidup yang mampu membuat makanan sendiri melalui fotosintesis, misalnya tumbuhan.
                    2. Konsumen: makhluk hidup yang memperoleh energi dengan memakan tumbuhan atau hewan lain. Konsumen dibedakan menjadi konsumen I, II, dan III.
                    3. Dekomposer: mikroorganisme yang menguraikan sisa makhluk hidup, misalnya bakteri dan jamur.

                    Rantai makanan
                    Padi --> Belalang --> Katak --> Ular --> Elang
                    Setiap perpindahan energi berarti sebagian energi berubah menjadi panas dan tidak pernah kembali ke organisme sebelumnya.

                    Contoh rantai makanan
                    Daun --> Wereng --> Katak --> Ular --> Elang.

                    Jaring makanan
                    Satu tumbuhan dapat menjadi bagian dari beberapa rantai makanan sekaligus. Contohnya, katak memakan wereng dan jangkrik, sedangkan ular memakan katak dan cacing.

                    Latihan
                    1. Jelaskan perbedaan rantai makanan dan jaring makanan.
                    2. Tentukan posisi konsumen I, konsumen II, dan konsumen III pada rantai: rumput --> kelinci --> rubah --> singa.
                    3. Jelaskan mengapa energi berpindah satu arah dan tidak pernah berputar kembali.
                    4. Berikan satu contoh peran dekomposer dalam rantai makanan.

                    Kunci jawaban
                    1. Rantai makanan merupakan satu jalur makanan, sedangkan jaring makanan merupakan gabungan beberapa rantai makanan yang saling berhubungan.
                    2. Konsumen I adalah kelinci, konsumen II adalah rubah, dan konsumen III adalah singa.
                    3. Karena pada setiap perpindahan energi sebagian energi berubah menjadi panas dan hilang ke lingkungan.
                    4. Dekomposer menguraikan sisa makhluk hidup sehingga unsur hara kembali ke tanah dan dapat dipakai tumbuhan."""));

    private static final List<TaskSeed> TASKS = List.of(
            new TaskSeed("Matematika", "Latihan Persamaan Linear",
                    "Kerjakan lima soal persamaan linear pada halaman 12. Tuliskan setiap langkah penyelesaian, lalu periksa kembali hasilmu dengan memasukkan nilai x ke persamaan awal. Kumpulkan catatan hasil latihan pada buku tulis.",
                    3, false),
            new TaskSeed("Matematika", "Latihan Himpunan dan Irisan",
                    "Tentukan gabungan, irisan, dan selisih dari dua himpunan yang diberikan pada lembar soal. Tambahkan satu soal cerita tentang himpunan di kelas.",
                    7, true),
            new TaskSeed("Bahasa Indonesia", "Tulis Teks Eksplanasi tentang Hujan",
                    "Susunlah teks eksplanasi sepanjang 150 sampai 200 kata yang menjelaskan proses terjadinya hujan. Teks wajib memuat kalimat utama, kalimat penjelas, dan kalimat penutup.",
                    5, false),
            new TaskSeed("Bahasa Indonesia", "Identifikasi Ciri Teks Eksplanasi",
                    "Bacalah tiga teks eksplanasi dari buku Bahasa Indonesia kelas VIII, lalu tandai kalimat utama dan kalimat penjelas pada setiap teks. Tulislah hasil identifikasinya.",
                    2, true),
            new TaskSeed("IPA", "Diagram Sistem Pernapasan",
                    "Buat diagram alur jalannya udara dari rongga hidung menuju alveolus. Sertakan keterangan fungsi setiap organ dan tanda panah arah aliran udara.",
                    4, false),
            new TaskSeed("IPA", "Rantai Makanan di Ekosistem",
                    "Tuliskan dua rantai makanan dari ekosistem sekitar tempat tinggalmu, kemudian gambarkan jaring makanan yang terbentuk dari kedua rantai tersebut.",
                    11, false),
            new TaskSeed("IPS", "Analisis Dampak Perubahan Sosial",
                    "Pilih satu bentuk perubahan sosial di lingkunganmu, lalu analisis dampak positif dan negatifnya. Sertakan sikap yang sebaiknya diambil masyarakat terhadap perubahan tersebut.",
                    9, true),
            new TaskSeed("Bahasa Inggris", "Write a Descriptive Text About Your Home",
                    "Write a descriptive text of 5 to 7 sentences about your house. Include the social paragraph, the elaboration paragraph, and the closing paragraph. Pay attention to the use of possessive pronouns and prepositions of place.",
                    6, false),
            new TaskSeed("Bahasa Inggris", "Vocabulary and Listening Quiz",
                    "Complete the vocabulary quiz about rooms and furniture, then answer the listening questions about a house description. Record your answer on the answer sheet provided.",
                    -2, false),
            new TaskSeed("PJOK", "Hitung Denyut Nadi Target",
                    "Hitung denyut nadi maksimum dan rentang denyut nadi target kamu, lalu susun program latihan aerobik selama satu minggu beserta alasannya.",
                    8, false),
            new TaskSeed("Seni Budaya", "Sketsa Motif Batik",
                    "Buat satu sketsa motif batik berukuran 10 x 10 cm pada kertas gambar. Motif wajib memuat bentuk dasar yang diulang-ulang dan diberi warna sesuai tema yang kamu pilih.",
                    10, true),
            new TaskSeed("Bahasa Indonesia", "Paragraf Argumentasi",
                    "Tulis satu paragraf argumentasi tentang pentingnya belajar di rumah dengan minimal empat kalimat. Sertakan satu alasan dan satu bukti yang relevan.",
                    1, false));

    private static final List<SessionSeed> SESSIONS = List.of(
            new SessionSeed("Matematika", 1, LocalTime.of(8, 0), 90, "Sesi pagi: latihan persamaan linear"),
            new SessionSeed("Bahasa Indonesia", 1, LocalTime.of(14, 0), 60, "Menulis teks eksplanasi"),
            new SessionSeed("IPA", 2, LocalTime.of(9, 30), 75, "Sistem pernapasan manusia"),
            new SessionSeed("Bahasa Inggris", 3, LocalTime.of(15, 0), 45, "Descriptive text: rumah"),
            new SessionSeed("PJOK", 4, LocalTime.of(7, 0), 60, "Latihan aerobik pagi"),
            new SessionSeed("IPS", 5, LocalTime.of(13, 30), 60, "Studi kasus perubahan sosial"),
            new SessionSeed("Matematika", 6, LocalTime.of(10, 0), 90, "Latihan himpunan"),
            new SessionSeed("Seni Budaya", -2, LocalTime.of(16, 0), 60, "Praktik menggambar motif"));

    private static final List<EvaluationSeed> EVALUATIONS = List.of(
            new EvaluationSeed("Matematika", -6, 86, 100,
                    "Pemahaman konsep persamaan linear sudah baik. Perlu latihan soal cerita yang lebih banyak."),
            new EvaluationSeed("Bahasa Indonesia", -5, 91, 100,
                    "Struktur teks eksplanasi sudah rapi dan kalimat penjelasnya jelas. Pertahankan."),
            new EvaluationSeed("IPA", -4, 78, 100,
                    "Nama organ sudah tepat, urutan jalannya udara perlu diulang agar lebih hafal."),
            new EvaluationSeed("Matematika", -3, 88, 100,
                    "Soal himpunan sudah dikuasai dengan baik. Latih kembali operasi komplemen."),
            new EvaluationSeed("Bahasa Inggris", -2, 84, 100,
                    "Teks deskriptif cukup baik. Gunakan lebih banyak kata sifat untuk memperjelas deskripsi."),
            new EvaluationSeed("PJOK", -1, 92, 100,
                    "Perhitungan denyut nadi tepat dan program latihan yang disusun realistis."),
            new EvaluationSeed("IPS", 0, 80, 100,
                    "Analisis sudah baik. Tambahkan contoh dampak yang terjadi di lingkungan sekitar."));

    private static final List<GoalSeed> GOALS = List.of(
            new GoalSeed("Menguasai persamaan linear satu variabel sampai tingkat soal cerita", 30, true),
            new GoalSeed("Menyusun teks eksplanasi minimal lima paragraf", 21, false),
            new GoalSeed("Memahami sistem pernapasan manusia dan dapat menjelaskannya", 14, true),
            new GoalSeed("Menulis teks deskriptif bahasa Inggris sepanjang tujuh kalimat", 7, false));

    /* ---------------- seeding ---------------- */

    /**
     * Creates the demo curriculum when it is missing. Nothing is overwritten and the seeding only
     * happens once per student.
     */
    public synchronized void seedIfNeeded() {
        try {
            ensureAccounts();
            connectAccounts();
            seedCurricula();
            saveMarkers();
        } catch (RuntimeException exception) {
            // A failure while seeding must never stop the application from starting.
            System.err.println("Data demo tidak dapat dibuat: " + exception.getMessage());
        }
    }

    private void ensureAccounts() {
        for (DemoAccount account : TEACHER_ACCOUNTS) {
            if (auth.findByEmail(account.email()).isPresent()) {
                markExistingAccount(account.email());
                continue;
            }
            auth.registerTeacher(account.name(), account.email(), account.password().toCharArray());
            markExistingAccount(account.email());
        }
        for (DemoAccount account : STUDENT_ACCOUNTS) {
            if (auth.findByEmail(account.email()).isPresent()) {
                markExistingAccount(account.email());
                continue;
            }
            auth.registerStudent(account.name(), account.email(), account.password().toCharArray(),
                    educationLevelOf(account.email()));
            markExistingAccount(account.email());
        }
    }

    private void markExistingAccount(String email) {
        auth.findByEmail(email).ifPresent(account -> markers.markAccount(account.getId()));
    }

    /** Creates the teacher/student connections, including one waiting and one rejected request. */
    private void connectAccounts() {
        connect(TEACHER_ACCOUNTS.get(0).email(), STUDENT_ACCOUNTS.get(0).email(), true);
        connect(TEACHER_ACCOUNTS.get(0).email(), STUDENT_ACCOUNTS.get(1).email(), true);
        connect(TEACHER_ACCOUNTS.get(0).email(), STUDENT_ACCOUNTS.get(2).email(), true);
        connect(TEACHER_ACCOUNTS.get(1).email(), STUDENT_ACCOUNTS.get(3).email(), true);
        connect(TEACHER_ACCOUNTS.get(2).email(), STUDENT_ACCOUNTS.get(2).email(), false);
        connect(TEACHER_ACCOUNTS.get(0).email(), STUDENT_ACCOUNTS.get(3).email(), false);
    }

    /**
     * Runs the invitation flow between two demo accounts.
     *
     * @param approve when true the request is approved so the connection becomes active
     */
    private void connect(String teacherEmail, String studentEmail, boolean approve) {
        if (!auth.findByEmail(teacherEmail).isPresent() || !auth.findByEmail(studentEmail).isPresent()) return;
        UserSession teacher = login(teacherEmail, TEACHER_PASSWORD);
        UserSession student = login(studentEmail, STUDENT_PASSWORD);
        if (teacher == null || student == null) return;
        boolean alreadyRequested = connections.requestsForStudent(student.token()).stream()
                .anyMatch(request -> request.teacherName().equals(teacherNameOf(teacherEmail)));
        if (alreadyRequested) return;
        try {
            CreatedInvitation invitation = connections.createInvitation(teacher.token());
            ConnectionRequestSummary request = connections.requestConnection(student.token(), invitation.code());
            if (approve) connections.approve(teacher.token(), request.id());
        } catch (RuntimeException exception) {
        }
    }

    private String teacherNameOf(String email) {
        return auth.findByEmail(email).map(account -> account.getName()).orElse(email);
    }

    private UserSession login(String email, String password) {
        try {
            return auth.login(email, password.toCharArray());
        } catch (RuntimeException exception) {
            return null;
        }
    }

    /** Creates the learning records for every student that does not have any yet. */
    private void seedCurricula() {
        for (int index = 0; index < STUDENT_ACCOUNTS.size(); index++) {
            DemoAccount account = STUDENT_ACCOUNTS.get(index);
            String studentAccountId = auth.findByEmail(account.email())
                    .map(id.edu.homeschool.model.UserAccount::getId).orElse(null);
            if (studentAccountId == null) continue;
            if (markers.isSeededStudent(studentAccountId)) continue;
            if (hasLearningRecord(studentAccountId)) {
                // The student already has data of their own, so nothing is added.
                markers.markSeededStudent(studentAccountId);
                continue;
            }
            String teacherEmail = index == STUDENT_ACCOUNTS.size() - 1
                    ? TEACHER_ACCOUNTS.get(1).email() : TEACHER_ACCOUNTS.get(0).email();
            UserSession teacher = login(teacherEmail, TEACHER_PASSWORD);
            UserSession student = login(account.email(), STUDENT_PASSWORD);
            if (teacher == null || student == null) continue;
            try {
                seedCurriculum(teacher, student, studentAccountId);
                markers.markSeededStudent(studentAccountId);
            } catch (RuntimeException exception) {
                System.err.println("Data demo siswa " + account.email() + " tidak lengkap: " + exception.getMessage());
            }
        }
    }

    private boolean hasLearningRecord(String studentAccountId) {
        return repositories.learningAccessGrants().findAll().stream()
                .anyMatch(grant -> studentAccountId.equals(grant.getStudentAccountId()));
    }

    private void seedCurriculum(UserSession teacher, UserSession student, String studentAccountId) {
        String token = teacher.token();
        String studentToken = student.token();
        LocalDate today = LocalDate.now();
        java.util.Map<String, String> subjectIds = new java.util.LinkedHashMap<>();
        for (SubjectSeed seed : SUBJECTS) {
            Subject subject = learning.saveSubject(token, studentAccountId, new Subject(seed.name(), seed.description()));
            subjectIds.put(seed.name(), subject.getId());
            markers.markResource(LearningWorkspaceService.TYPE_SUBJECT, subject.getId());
        }
        for (MaterialSeed seed : MATERIALS) {
            String subjectId = subjectIds.get(seed.subject());
            if (subjectId == null) continue;
            LearningMaterial material = learning.saveMaterial(token, studentAccountId, new LearningMaterial(subjectId,
                    seed.title(), seed.type(), seed.location(), seed.notes(), seed.objectives(), seed.content()));
            markers.markResource(LearningWorkspaceService.TYPE_MATERIAL, material.getId());
        }
        for (TaskSeed seed : TASKS) {
            String subjectId = subjectIds.get(seed.subject());
            if (subjectId == null) continue;
            LearningTask task = learning.saveTask(token, studentAccountId,
                    new LearningTask(seed.title(), subjectId, today.plusDays(seed.dueOffset()), seed.instructions()));
            if (seed.completed()) learning.setTaskCompleted(studentToken, task.getId(), true);
            markers.markResource(LearningWorkspaceService.TYPE_TASK, task.getId());
        }
        for (SessionSeed seed : SESSIONS) {
            String subjectId = subjectIds.get(seed.subject());
            if (subjectId == null) continue;
            StudySession session = learning.saveSession(token, studentAccountId, new StudySession(subjectId,
                    today.plusDays(seed.dayOffset()), seed.time(), seed.duration(), seed.notes()));
            markers.markResource(LearningWorkspaceService.TYPE_SESSION, session.getId());
        }
        for (EvaluationSeed seed : EVALUATIONS) {
            String subjectId = subjectIds.get(seed.subject());
            if (subjectId == null) continue;
            Evaluation evaluation = learning.saveEvaluation(token, studentAccountId, new Evaluation(subjectId,
                    today.plusDays(seed.dayOffset()), seed.score(), seed.maximum(), seed.notes()));
            markers.markResource(LearningWorkspaceService.TYPE_EVALUATION, evaluation.getId());
        }
        for (GoalSeed seed : GOALS) {
            LearningGoal goal = learning.saveGoal(token, studentAccountId,
                    new LearningGoal(seed.description(), today.plusDays(seed.targetOffset())));
            if (seed.achieved()) learning.setGoalAchieved(token, goal.getId(), true);
            markers.markResource(LearningWorkspaceService.TYPE_GOAL, goal.getId());
        }
    }

    private static String educationLevelOf(String email) {
        return switch (email) {
            case "dimas@demo.test" -> "SMA";
            case "nadia@demo.test" -> "SMP";
            case "yusuf@demo.test" -> "SMP";
            case "aulia@demo.test" -> "SD";
            default -> "SMA";
        };
    }

    /* ---------------- markers ---------------- */

    private void loadMarkers() {
        try {
            markers = repositories.demoMarkers().load();
        } catch (RuntimeException exception) {
            markers = new DemoMarkers();
        }
    }

    private void saveMarkers() {
        try {
            repositories.demoMarkers().save(markers);
        } catch (RuntimeException exception) {
            System.err.println("Penanda data demo tidak dapat disimpan: " + exception.getMessage());
        }
    }

    /* ---------------- queries ---------------- */

    /** True when the account was created by the demo seeder. */
    public boolean isDemoAccount(String accountId) {
        return markers.isAccount(accountId);
    }

    /** True when the learning record was created by the demo seeder. */
    public boolean isDemoResource(String type, String resourceId) {
        return markers.isResource(type, resourceId);
    }

    /** The demo accounts that exist in the current data directory, for the login screen. */
    public List<DemoAccount> demoAccounts() {
        List<DemoAccount> available = new ArrayList<>();
        for (DemoAccount account : TEACHER_ACCOUNTS) {
            if (auth.findByEmail(account.email()).isPresent()) available.add(account);
        }
        for (DemoAccount account : STUDENT_ACCOUNTS) {
            if (auth.findByEmail(account.email()).isPresent()) available.add(account);
        }
        return List.copyOf(available);
    }

    /** The learning record ids that belong to the demo data. */
    public List<String> demoResourceIds(String type) {
        List<String> ids = markers.getResources().get(type);
        return ids == null ? List.of() : List.copyOf(ids);
    }
}
