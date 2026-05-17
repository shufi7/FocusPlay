package com.example.focusplay.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.focusplay.R

class DashboardAnakActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_anak)

        val tvWelcomeAnak = findViewById<TextView>(R.id.tvWelcomeAnak)
        val btnKembaliKeOrtu = findViewById<Button>(R.id.btnKembaliKeOrtu)

        // Tangkap data dari Orang Tua
        val idAnak = intent.getStringExtra("ID_ANAK") ?: ""
        val namaAnak = intent.getStringExtra("NAMA_ANAK") ?: "Anak Hebat"
        tvWelcomeAnak.text = "Halo, $namaAnak!"

        // Tombol Keluar dari Area Anak
        btnKembaliKeOrtu.setOnClickListener {
            finish()
        }

        // --- SISTEM TOMBOL PERMAINAN (DENGAN ALIRAN ID ANAK) ---
        findViewById<CardView>(R.id.cardGame1).setOnClickListener {
            val intent = Intent(this, com.example.focusplay.view.games.GameTapMerahActivity::class.java)
            intent.putExtra("ID_ANAK", idAnak) // Lempar ID Anak ke dalam Game
            startActivity(intent)
        }

        findViewById<CardView>(R.id.cardGame2).setOnClickListener {
            val intent = Intent(this, com.example.focusplay.view.games.GameAntarRumahActivity::class.java)
            intent.putExtra("ID_ANAK", idAnak)
            startActivity(intent)
        }

        findViewById<CardView>(R.id.cardGame3).setOnClickListener {
            val intent = Intent(this, com.example.focusplay.view.games.GamePasangKartuActivity::class.java)
            intent.putExtra("ID_ANAK", idAnak)
            startActivity(intent)
        }

        findViewById<CardView>(R.id.cardGame4).setOnClickListener {
            val intent = Intent(this, com.example.focusplay.view.games.GameUrutkanAngkaActivity::class.java)
            intent.putExtra("ID_ANAK", idAnak)
            startActivity(intent)
        }

        findViewById<CardView>(R.id.cardGame5).setOnClickListener {
            val intent = Intent(this, com.example.focusplay.view.games.GameTangkapWarnaActivity::class.java)
            intent.putExtra("ID_ANAK", idAnak)
            startActivity(intent)
        }
    }
}