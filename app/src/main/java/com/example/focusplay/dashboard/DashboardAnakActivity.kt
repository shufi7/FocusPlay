package com.example.focusplay.dashboard

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.focusplay.R
import com.example.focusplay.games.GameAntarRumahActivity
import com.example.focusplay.games.GamePasangKartuActivity
import com.example.focusplay.games.GameUrutkanAngkaActivity

class DashboardAnakActivity : AppCompatActivity() {

    private var idAnak: String = ""
    private var namaAnak: String = "Anak Hebat"
    private var usiaAnak: Int = 0

    private lateinit var tvWelcomeAnak: TextView
    private lateinit var btnKembaliKeOrtu: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_anak)

        ambilDataAnakDariIntent()
        hubungkanView()
        tampilkanDataAnak()
        aturTombol()
    }

    private fun ambilDataAnakDariIntent() {
        idAnak = intent.getStringExtra("ID_ANAK")
            ?: intent.getStringExtra("id_anak")
                    ?: ""

        namaAnak = intent.getStringExtra("NAMA_ANAK")
            ?: intent.getStringExtra("nama_anak")
                    ?: "Anak Hebat"

        usiaAnak = intent.getIntExtra(
            "USIA_ANAK",
            intent.getIntExtra("usia_anak", 0)
        )
    }

    private fun hubungkanView() {
        tvWelcomeAnak = findViewById(R.id.tvWelcomeAnak)
        btnKembaliKeOrtu = findViewById(R.id.btnKembaliKeOrtu)
    }

    private fun tampilkanDataAnak() {
        tvWelcomeAnak.text = "Halo, $namaAnak!"
    }

    private fun aturTombol() {
        btnKembaliKeOrtu.setOnClickListener {
            finish()
        }

        findViewById<CardView>(R.id.cardGame1).setOnClickListener {
            bukaGame(GameAntarRumahActivity::class.java)
        }

        findViewById<CardView>(R.id.cardGame2).setOnClickListener {
            bukaGame(GamePasangKartuActivity::class.java)
        }

        findViewById<CardView>(R.id.cardGame3).setOnClickListener {
            bukaGame(GameUrutkanAngkaActivity::class.java)
        }
    }

    private fun bukaGame(targetActivity: Class<*>) {
        if (idAnak.isEmpty()) {
            Toast.makeText(
                this,
                "Data profil anak belum terbaca. Silakan pilih profil anak ulang.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val intent = Intent(this, targetActivity)

        intent.putExtra("ID_ANAK", idAnak)
        intent.putExtra("NAMA_ANAK", namaAnak)
        intent.putExtra("USIA_ANAK", usiaAnak)

        intent.putExtra("id_anak", idAnak)
        intent.putExtra("nama_anak", namaAnak)
        intent.putExtra("usia_anak", usiaAnak)

        startActivity(intent)
    }
}