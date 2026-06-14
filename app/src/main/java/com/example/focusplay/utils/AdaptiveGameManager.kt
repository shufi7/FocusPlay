package com.example.focusplay.utils

/**
 * Mengatur naik-turunnya tingkat kesulitan permainan.
 *
 * Activity game mengirim hasil benar atau salah ke prosesJawaban(). Fase hasilnya digunakan
 * untuk menentukan jumlah objek atau tingkat kesulitan sesi berikutnya.
 */
class AdaptiveGameManager(
    // Menyimpan fase aktif dan dapat berubah selama permainan.
    private var faseSekarang: Int = 1,
    // Menentukan apakah perubahan fase otomatis digunakan.
    private val modeAdaptifAktif: Boolean = true
) {

    // ==================== BAGIAN HITUNGAN JAWABAN ====================
    // Menghitung jawaban atau sesi yang berhasil.
    private var jumlahBenar = 0
    // Menghitung jawaban atau sesi yang gagal.
    private var jumlahSalah = 0

    init {
        // Memastikan mode nonadaptif selalu dimulai pada fase termudah.
        if (!modeAdaptifAktif) {
            faseSekarang = 1
        }
    }

    // ==================== BAGIAN PROSES ADAPTIF ====================
    fun prosesJawaban(jawabanBenar: Boolean): Int {
        // Jika mode adaptif dimatikan, permainan dikunci pada fase 1.
        if (!modeAdaptifAktif) {
            faseSekarang = 1
            return 1
        }

        if (jawabanBenar) {
            // Menambah penghitung keberhasilan.
            jumlahBenar++

            if (jumlahBenar >= 5) {
                // Lima kali benar akan menaikkan fase.
                naikFase()
                // Mengosongkan hitungan setelah batas perubahan fase tercapai.
                resetHitungan()
            }
        } else {
            // Menambah penghitung kesalahan.
            jumlahSalah++

            if (jumlahSalah >= 3) {
                // Tiga kali salah akan menurunkan fase agar permainan lebih mudah.
                turunFase()
                resetHitungan()
            }
        }

        // Mengembalikan fase terbaru ke Activity game.
        return faseSekarang
    }

    // ==================== BAGIAN PERUBAHAN FASE ====================
    private fun naikFase() {
        // Fase maksimum dibatasi sampai fase 3.
        if (faseSekarang < 3) {
            faseSekarang++
        }
    }

    private fun turunFase() {
        // Fase minimum dibatasi sampai fase 1.
        if (faseSekarang > 1) {
            faseSekarang--
        }
    }

    // ==================== BAGIAN STATUS DAN RESET ====================
    fun getFaseSekarang(): Int {
        // Mengembalikan fase tanpa memproses jawaban baru.
        return faseSekarang
    }

    fun resetHitungan() {
        // Mengosongkan kedua penghitung untuk penilaian berikutnya.
        jumlahBenar = 0
        jumlahSalah = 0
    }
}
