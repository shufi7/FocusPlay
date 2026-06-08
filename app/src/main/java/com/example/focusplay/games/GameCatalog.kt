package com.example.focusplay.games

import androidx.annotation.DrawableRes
import com.example.focusplay.R

data class GameInfo(
    val key: String,
    val title: String,
    @DrawableRes val coverRes: Int,
    val dashboardDescription: String,
    val goal: String,
    val howToPlay: String
)

object GameCatalog {
    val dashboardGames = listOf(
        get("antar_rumah"),
        get("pasang_kartu"),
        get("urut_angka")
    )

    fun get(gameKey: String): GameInfo {
        return when (gameKey) {
            "pasang_kartu" -> GameInfo(
                key = "pasang_kartu",
                title = "Pasang Kartu",
                coverRes = R.drawable.cover_pasang_kartu,
                dashboardDescription = "Cocokkan kartu yang sama untuk melatih memori dan konsentrasi.",
                goal = "Melatih daya ingat, konsentrasi, dan kemampuan mencocokkan pasangan gambar.",
                howToPlay = "1. Ingat posisi gambar saat seluruh kartu masih terbuka.\n" +
                        "2. Setelah beberapa detik, seluruh kartu akan tertutup.\n" +
                        "3. Buka dua kartu dan cocokkan gambar yang sama.\n" +
                        "4. Setelah satu sesi selesai, kartu baru akan muncul kembali dari tahap mengingat."
            )

            "urut_angka" -> GameInfo(
                key = "urut_angka",
                title = "Urutkan Angka",
                coverRes = R.drawable.bg_antar_si_domba,
                dashboardDescription = "Susun angka secara berurutan untuk melatih logika sederhana.",
                goal = "Melatih fokus, logika berpikir, dan kemampuan mengenali urutan angka.",
                howToPlay = "1. Perhatikan angka yang muncul di layar.\n" +
                        "2. Susun angka dari urutan yang benar.\n" +
                        "3. Pilih dengan hati-hati agar skor bertambah.\n" +
                        "4. Selesaikan permainan sebelum waktu habis."
            )

            else -> GameInfo(
                key = "antar_rumah",
                title = "Antar Si Domba",
                coverRes = R.drawable.bg_antar_si_domba,
                dashboardDescription = "Seret domba ke rumah dengan warna yang sesuai untuk melatih koordinasi visual.",
                goal = "Melatih fokus, ketelitian, koordinasi tangan dan mata, serta kemampuan mencocokkan warna.",
                howToPlay = "1. Perhatikan domba yang muncul di area permainan.\n" +
                        "2. Seret domba ke rumah yang memiliki warna yang sama.\n" +
                        "3. Jika jawaban benar, skor akan bertambah.\n" +
                        "4. Jika mode adaptif aktif, fase permainan akan naik atau turun sesuai hasil jawaban."
            )
        }
    }
}
