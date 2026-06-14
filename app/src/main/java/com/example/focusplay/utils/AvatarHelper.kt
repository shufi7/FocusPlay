package com.example.focusplay.utils

import androidx.annotation.DrawableRes
import com.example.focusplay.R

/**
 * Memetakan ID avatar berbentuk String ke gambar yang ada di res/drawable.
 *
 * avatarIds dipakai TambahAnakActivity untuk membuat pilihan. getAvatarResource() dipakai
 * PilihAnakActivity, AnakAdapter, dan DashboardActivity untuk menampilkan gambarnya.
 */
object AvatarHelper {
    // ==================== BAGIAN DAFTAR AVATAR ====================
    // String pada daftar ini disimpan ke field "avatar" pada data anak di Firestore.
    // Urutan daftar juga menentukan urutan pilihan pada TambahAnakActivity.
    val avatarIds = listOf(
        "char_red",
        "char_blue",
        "char_purple",
        "char_star",
        "char_mushroom",
        "char_moon_purple",
        "char_cucumber",
        "char_cloud_blue",
        "char_heart",
        "char_diamond_orange"
    )

    // ==================== BAGIAN PEMETAAN GAMBAR AVATAR ====================
    @DrawableRes
    fun getAvatarResource(avatar: String?): Int {
        // Menggunakan char_red jika avatar kosong atau tidak dikenali.
        // when mencocokkan ID String dengan resource drawable yang sesuai.
        return when (avatar) {
            "char_red" -> R.drawable.char_red
            "char_blue" -> R.drawable.char_blue
            "char_purple" -> R.drawable.char_purple
            "char_star" -> R.drawable.char_star
            "char_mushroom" -> R.drawable.char_mushroom

            "char_moon_purple" -> R.drawable.char_moon_purple
            "char_cucumber" -> R.drawable.char_cucumber
            "char_cloud_blue" -> R.drawable.char_cloud_blue
            "char_heart" -> R.drawable.char_heart
            "char_diamond_orange" -> R.drawable.char_diamond_orange

            else -> R.drawable.char_red
        }
    }
}
