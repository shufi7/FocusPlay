package com.example.focusplay.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import com.example.focusplay.profile.PilihPeranActivity
import com.example.focusplay.utils.SessionManager

/**
 * Halaman pembuka aplikasi dengan tampilan activity_splash.xml.
 *
 * Setelah splash selesai, status login diperiksa melalui SessionManager.
 * Pengguna yang masih login diarahkan ke PilihPeranActivity, selain itu ke AuthChoiceActivity.
 */
class SplashActivity : AppCompatActivity() {

    // ==================== BAGIAN VARIABEL SPLASH ====================
    // Menyediakan akses ke data status login yang tersimpan secara lokal.
    private lateinit var session: SessionManager
    // Menentukan lama halaman splash tampil dalam satuan milidetik.
    private val splashDelay = 2500L
    // Menjalankan perpindahan halaman tertunda pada main thread.
    private val handler = Handler(Looper.getMainLooper())

    // ==================== BAGIAN INISIALISASI HALAMAN ====================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Memasang activity_splash.xml sebagai tampilan halaman.
        setContentView(R.layout.activity_splash)

        // Menyembunyikan ActionBar agar splash tampil penuh.
        supportActionBar?.hide()

        // Membuat SessionManager menggunakan context SplashActivity.
        session = SessionManager(this)

        // Mengambil elemen logo dari XML.
        val ivLogoSplash = findViewById<ImageView>(R.id.ivLogoSplash)
        // Memulai animasi logo setelah elemen ditemukan.
        mulaiAnimasiLogo(ivLogoSplash)

        // Memberi jeda agar animasi splash terlihat sebelum berpindah halaman.
        handler.postDelayed({
            // Menentukan Activity tujuan berdasarkan status login.
            val tujuan = if (session.isLogin()) {
                Intent(this, PilihPeranActivity::class.java)
            } else {
                Intent(this, AuthChoiceActivity::class.java)
            }

            // Membuka halaman tujuan yang sudah ditentukan.
            startActivity(tujuan)
            // Menutup splash agar tidak muncul saat tombol kembali ditekan.
            finish()
        }, splashDelay)
    }

    // ==================== BAGIAN ANIMASI LOGO ====================
    private fun mulaiAnimasiLogo(view: ImageView) {
        // Animasi Muncul (Fade In)
        // Mengatur logo tidak terlihat sebagai kondisi awal fade-in.
        view.alpha = 0f
        // Membuat animasi perubahan alpha sampai logo terlihat.
        view.animate()
            .alpha(1f)
            .setDuration(800)
            .start()

        // Animasi Naik Turun (Bobbing)
        // Menentukan jarak gerakan vertikal logo.
        val movingDistance = 30f // Jarak gerak 30px
        
        view.animate()
            .translationY(movingDistance)
            .setDuration(1200)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction(object : Runnable {
                override fun run() {
                    animasiKebalikan(view, movingDistance)
                }
            })
            .start()
    }

    private fun animasiKebalikan(view: ImageView, distance: Float) {
        // Dipanggil berulang agar logo terus bergerak naik dan turun.
        // Menentukan arah berikutnya berdasarkan posisi logo saat ini.
        val target = if (view.translationY > 0) -distance else distance
        
        view.animate()
            .translationY(target)
            .setDuration(1200)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                animasiKebalikan(view, distance)
            }
            .start()
    }

    // ==================== BAGIAN PEMBERSIHAN PROSES ====================
    override fun onDestroy() {
        super.onDestroy()
        // Menghapus proses tertunda agar tidak berjalan setelah halaman splash ditutup.
        handler.removeCallbacksAndMessages(null)
    }
}
