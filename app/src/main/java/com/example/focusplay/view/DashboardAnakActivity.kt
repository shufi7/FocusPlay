package com.example.focusplay.view

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R

class DashboardAnakActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_anak)

        val tvWelcomeAnak = findViewById<TextView>(R.id.tvWelcomeAnak)
        val btnKembaliKeOrtu = findViewById<Button>(R.id.btnKembaliKeOrtu)

        // Tangkap data nama anak yang dikirim dari Dashboard Orang Tua
        val namaAnak = intent.getStringExtra("NAMA_ANAK") ?: "Anak Hebat"
        tvWelcomeAnak.text = "Halo, $namaAnak!"

        // Tombol untuk menutup area anak
        btnKembaliKeOrtu.setOnClickListener {
            finish()
        }
    }
}