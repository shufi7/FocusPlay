package com.example.focusplay.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("FocusPlayPrefs", Context.MODE_PRIVATE)

    fun simpanSesiLogin(id: Int, nama: String, email: String) {
        val editor = prefs.edit()
        editor.putBoolean("IS_LOGGED_IN", true)
        editor.putInt("USER_ID", id) // Menyimpan ID ke brankas
        editor.putString("USER_NAMA", nama)
        editor.putString("USER_EMAIL", email)
        editor.apply()
    }

    fun isLogin(): Boolean {
        return prefs.getBoolean("IS_LOGGED_IN", false)
    }

    // --- TAMBAHKAN FUNGSI INI ---
    fun getUserId(): Int {
        return prefs.getInt("USER_ID", 0) // Mengambil ID dari brankas, default 0 jika tidak ada
    }

    fun getNamaUser(): String? {
        return prefs.getString("USER_NAMA", "Pengguna")
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}