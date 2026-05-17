package com.example.focusplay.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val pref: SharedPreferences =
        context.getSharedPreferences("focusplay_session", Context.MODE_PRIVATE)

    private val editor: SharedPreferences.Editor = pref.edit()

    companion object {
        private const val KEY_IS_LOGIN = "is_login"
        private const val KEY_ID_PENDAMPING = "id_pendamping"
        private const val KEY_NAMA_PENDAMPING = "nama_pendamping"
        private const val KEY_EMAIL = "email"
    }

    fun simpanSesiLogin(
        idPendamping: Int,
        namaPendamping: String,
        email: String
    ) {
        editor.putBoolean(KEY_IS_LOGIN, true)
        editor.putInt(KEY_ID_PENDAMPING, idPendamping)
        editor.putString(KEY_NAMA_PENDAMPING, namaPendamping)
        editor.putString(KEY_EMAIL, email)
        editor.apply()
    }

    fun isLogin(): Boolean {
        return pref.getBoolean(KEY_IS_LOGIN, false)
    }

    fun getIdPendamping(): Int {
        return pref.getInt(KEY_ID_PENDAMPING, 0)
    }

    // Tambahan agar cocok dengan TambahAnakActivity.kt
    fun getUserId(): Int {
        return getIdPendamping()
    }

    fun getNamaPendamping(): String {
        return pref.getString(KEY_NAMA_PENDAMPING, "") ?: ""
    }

    // Tambahan agar cocok dengan DashboardActivity.kt
    fun getNamaUser(): String {
        return getNamaPendamping()
    }

    fun getEmail(): String {
        return pref.getString(KEY_EMAIL, "") ?: ""
    }

    fun logout() {
        editor.clear()
        editor.apply()
    }
    // Fungsi untuk menyimpan PIN
    fun simpanPin(pin: String) {
        editor.putString("PIN_ORTU", pin)
        editor.apply()
    }

    // Fungsi untuk mengambil PIN
    fun getPin(): String {
        return pref.getString("PIN_ORTU", "") ?: ""
    }
}