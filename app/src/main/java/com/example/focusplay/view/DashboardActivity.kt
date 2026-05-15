package com.example.focusplay.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import com.example.focusplay.utils.SessionManager

class DashboardActivity : AppCompatActivity() {

    private lateinit var tvWelcomeName: TextView
    private lateinit var btnLogout: Button
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Hubungkan variabel dengan UI
        tvWelcomeName = findViewById(R.id.tvWelcomeName)
        btnLogout = findViewById(R.id.btnLogout)

        // Inisialisasi SessionManager
        session = SessionManager(this)

        // Ambil nama dari brankas sesi dan tampilkan
        val namaUser = session.getNamaUser()
        tvWelcomeName.text = "Halo, $namaUser!"

        // Beri perintah untuk tombol Keluar
        btnLogout.setOnClickListener {
            // 1. Hapus isi brankas sesi
            session.logout()

            // 2. Lempar kembali ke halaman Login
            val intent = Intent(this, LoginActivity::class.java)
            // Hapus riwayat halaman ini agar pengguna tidak bisa menekan tombol "Back" di HP untuk kembali ke Dashboard
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}