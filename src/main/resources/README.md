# Logo aplikasi

Folder ini disiapkan untuk berkas gambar logo resmi.

## Cara memakai artwork PNG asli

Letakkan berkas bernama **`logo.png`** tepat di folder ini:

```text
src/main/resources/logo.png
```

 lalu build ulang (`mvnw.cmd clean test` atau `mvnw.cmd exec:java`).

Aplikasi akan mendeteksi berkas tersebut secara otomatis dan menampilkannya
di layar masuk dan di sidebar — **tanpa perlu mengubah satu baris kode**.

## Jika folder ini kosong

Aplikasi tetap berjalan normal. Logo digambar secara vektor oleh
`id.edu.homeschool.ui.design.AppLogo`, sehingga:

- project tidak memerlukan dependensi gambar tambahan,
- logo tetap tajam pada monitor resolusi tinggi maupun skalasi DPI,
- tidak ada berkas biner yang perlu ikut versioning.

## Ukuran yang disarankan

| Pemakaian | Ukuran |
|---|---|
| Ikon jendela (`.ico`) | 64 x 64 atau 256 x 256 piksel |
| Square mark di sidebar | 34 x 34 piksel |
| Lockup di layar masuk | lebar minimal 320 piksel |

Gunakan latar transparan (PNG dengan alpha) agar menyatu dengan permukaan
"glass" aplikasi.
