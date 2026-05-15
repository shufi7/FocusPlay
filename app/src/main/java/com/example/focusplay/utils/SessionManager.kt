package com.example.focusplay.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    // Membuat atau membuka brankas penyimpanan bernama "FocusPlayPrefs"
    private val prefs: SharedPreferences = context.getSharedPreferences("FocusPlayPrefs", Context.MODE_PRIVATE)

    // Fungsi untuk menyimpan data saat berhasil login
    fun simpanSesiLogin(id: Int, nama: String, email: String) {
        val editor = prefs.edit()
        editor.putBoolean("IS_LOGGED_IN", true) // Menandai bahwa ada yang sedang login
        editor.putInt("USER_ID", id)
        editor.putString("USER_NAMA", nama)
        editor.putString("USER_EMAIL", email)
        editor.apply() // Kunci brankasnya
    }

    // Fungsi untuk mengecek apakah sedang ada yang login
    fun isLogin(): Boolean {
        return prefs.getBoolean("IS_LOGGED_IN", false)
    }

    // Fungsi untuk mengambil nama pengguna yang sedang login
    fun getNamaUser(): String? {
        return prefs.getString("USER_NAMA", "Pengguna")
    }

    // Fungsi untuk menghapus sesi (Logout)
    fun logout() {
        prefs.edit().clear().apply() // Kosongkan brankas
    }
}