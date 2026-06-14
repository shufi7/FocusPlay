package com.example.focusplay.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Mengurus SharedPreferences bernama "focusplay_session".
 *
 * Data sesi masuk dari proses login, lalu dipakai lagi di SplashActivity untuk menentukan
 * halaman tujuan dan di DashboardActivity saat pengguna logout.
 */
class SessionManager(context: Context) {

    // ==================== BAGIAN PENYIMPANAN SESI ====================
    // MODE_PRIVATE membuat data sesi hanya dapat diakses oleh aplikasi sendiri.
    // File "focusplay_session" menjadi tempat penyimpanan seluruh data sesi.
    private val pref: SharedPreferences =
        context.getSharedPreferences("focusplay_session", Context.MODE_PRIVATE)

    // Editor digunakan untuk menambah, mengubah, dan menghapus data preferences.
    private val editor: SharedPreferences.Editor = pref.edit()

    // ==================== BAGIAN KEY SHARED PREFERENCES ====================
    companion object {
        // Key status apakah pengguna sudah login.
        private const val KEY_IS_LOGIN = "is_login"
        // Key ID akun pendamping.
        private const val KEY_ID_PENDAMPING = "id_pendamping"
        // Key nama akun pendamping.
        private const val KEY_NAMA_PENDAMPING = "nama_pendamping"
        // Key email akun pendamping.
        private const val KEY_EMAIL = "email"
    }

    // ==================== BAGIAN SIMPAN DATA LOGIN ====================
    fun simpanSesiLogin(
        idPendamping: Int,
        namaPendamping: String,
        email: String
    ) {
        // Menyimpan status login dan identitas pendamping untuk digunakan halaman lain.
        // Menandai sesi sebagai sudah login.
        editor.putBoolean(KEY_IS_LOGIN, true)
        // Menyimpan ID pendamping.
        editor.putInt(KEY_ID_PENDAMPING, idPendamping)
        // Menyimpan nama pendamping.
        editor.putString(KEY_NAMA_PENDAMPING, namaPendamping)
        // Menyimpan email pendamping.
        editor.putString(KEY_EMAIL, email)
        // Menerapkan semua perubahan secara asynchronous.
        editor.apply()
    }

    // ==================== BAGIAN AMBIL DATA SESI ====================
    fun isLogin(): Boolean {
        // Nilai ini nanti diambil SplashActivity; kalau belum ada sesi hasil awalnya false.
        // Mengambil status login dengan nilai default false.
        return pref.getBoolean(KEY_IS_LOGIN, false)
    }

    fun getIdPendamping(): Int {
        // Mengambil ID pendamping dengan nilai default 0.
        return pref.getInt(KEY_ID_PENDAMPING, 0)
    }

    // Tambahan agar cocok dengan TambahAnakActivity.kt
    fun getUserId(): Int {
        // Alias getIdPendamping() untuk pemanggilan yang memakai nama getUserId().
        return getIdPendamping()
    }

    fun getNamaPendamping(): String {
        // Mengambil nama pendamping dan menghindari hasil null.
        return pref.getString(KEY_NAMA_PENDAMPING, "") ?: ""
    }

    // Tambahan agar cocok dengan DashboardActivity.kt
    fun getNamaUser(): String {
        // Alias getNamaPendamping() untuk pemanggilan dari Dashboard.
        return getNamaPendamping()
    }

    fun getEmail(): String {
        // Mengambil email pendamping dan menghindari hasil null.
        return pref.getString(KEY_EMAIL, "") ?: ""
    }

    // ==================== BAGIAN LOGOUT ====================
    fun logout() {
        // Fungsi ini dipanggil dari DashboardActivity sebelum kembali ke halaman autentikasi.
        // Menghapus seluruh nilai pada file sesi.
        editor.clear()
        // Menerapkan penghapusan data sesi.
        editor.apply()
    }
    // ==================== BAGIAN PIN ORANG TUA ====================
    // Fungsi untuk menyimpan PIN
    fun simpanPin(pin: String) {
        // PIN orang tua simpan di SharedPreferences sesi yang sama.
        // Menyimpan PIN menggunakan key khusus PIN_ORTU.
        editor.putString("PIN_ORTU", pin)
        editor.apply()
    }

    // Fungsi untuk mengambil PIN
    fun getPin(): String {
        // Mengambil PIN, atau String kosong jika belum disimpan.
        return pref.getString("PIN_ORTU", "") ?: ""
    }
}
