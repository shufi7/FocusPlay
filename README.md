# FocusPlay

## Konfigurasi API AI

Project ini membaca API key FreeModel dari variabel `FREEMODEL_API_KEY`.
Urutan yang didukung ada tiga:

1. Gradle property `FREEMODEL_API_KEY`
2. Environment variable `FREEMODEL_API_KEY`
3. File `local.properties`

Untuk teman yang clone dari GitHub, cara paling mudah:

1. Salin `local.properties.example` menjadi `local.properties`
2. Isi nilainya:

```properties
FREEMODEL_API_KEY=isi_api_key_freemodel_di_sini
```

File `local.properties` sudah masuk `.gitignore`, jadi key tidak ikut ter-upload ke GitHub.

Untuk build otomatis di GitHub Actions, simpan key sebagai repository secret bernama
`FREEMODEL_API_KEY`, lalu jalankan Gradle dengan environment variable tersebut.

Catatan penting: jangan commit API key asli ke repository. API key yang masuk ke repo publik
atau APK Android bisa dibaca orang lain, jadi lebih aman setiap developer memakai secret lokal
atau secret repository.
