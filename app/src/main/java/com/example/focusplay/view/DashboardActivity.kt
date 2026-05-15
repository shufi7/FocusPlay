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
    private lateinit var btnKeTambahAnak: Button // Variabel baru
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        tvWelcomeName = findViewById(R.id.tvWelcomeName)
        btnLogout = findViewById(R.id.btnLogout)
        btnKeTambahAnak = findViewById(R.id.btnKeTambahAnak) // Hubungkan ID

        session = SessionManager(this)

        val namaUser = session.getNamaUser()
        tvWelcomeName.text = "Halo, $namaUser!"

        // Pindah ke halaman Tambah Anak
        btnKeTambahAnak.setOnClickListener {
            startActivity(Intent(this, TambahAnakActivity::class.java))
        }

        btnLogout.setOnClickListener {
            session.logout()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}