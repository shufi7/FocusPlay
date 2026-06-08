package com.example.focusplay.dashboard

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.focusplay.R
import com.example.focusplay.games.GameCatalog
import com.example.focusplay.games.GameDescriptionActivity
import com.example.focusplay.games.GameInfo
import com.example.focusplay.utils.AvatarHelper

class DashboardAnakActivity : AppCompatActivity() {

    private var idAnak: String = ""
    private var namaAnak: String = "Anak Hebat"
    private var usiaAnak: Int = 0
    private var avatarAnak: String = "char_red"

    private lateinit var tvWelcomeAnak: TextView
    private lateinit var imgAvatarAnak: android.widget.ImageView
    private lateinit var btnKembaliKeOrtu: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_anak)

        ambilDataAnakDariIntent()
        hubungkanView()
        tampilkanDataAnak()
        tampilkanDaftarGame()
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

        avatarAnak = intent.getStringExtra("AVATAR_ANAK")
            ?: intent.getStringExtra("avatar_anak")
                    ?: "char_red"
    }

    private fun hubungkanView() {
        tvWelcomeAnak = findViewById(R.id.tvWelcomeAnak)
        imgAvatarAnak = findViewById(R.id.imgAvatarAnakDashboard)
        btnKembaliKeOrtu = findViewById(R.id.btnKembaliKeOrtu)
    }

    private fun tampilkanDataAnak() {
        tvWelcomeAnak.text = "Halo, $namaAnak! Mau main apa?"

        imgAvatarAnak.setImageResource(AvatarHelper.getAvatarResource(avatarAnak))
    }

    private fun tampilkanDaftarGame() {
        val gameViews = listOf(
            GameCardViews(
                cover = findViewById(R.id.imgCoverGame1),
                title = findViewById(R.id.tvTitleGame1),
                description = findViewById(R.id.tvDescriptionGame1)
            ),
            GameCardViews(
                cover = findViewById(R.id.imgCoverGame2),
                title = findViewById(R.id.tvTitleGame2),
                description = findViewById(R.id.tvDescriptionGame2)
            ),
            GameCardViews(
                cover = findViewById(R.id.imgCoverGame3),
                title = findViewById(R.id.tvTitleGame3),
                description = findViewById(R.id.tvDescriptionGame3)
            )
        )

        GameCatalog.dashboardGames.zip(gameViews).forEach { (gameInfo, views) ->
            views.tampilkan(gameInfo)
        }
    }

    private fun aturTombol() {
        btnKembaliKeOrtu.setOnClickListener {
            finish()
        }

        findViewById<CardView>(R.id.cardGame1).setOnClickListener {
            bukaDeskripsiGame("antar_rumah")
        }

        findViewById<CardView>(R.id.cardGame2).setOnClickListener {
            bukaDeskripsiGame("pasang_kartu")
        }

        findViewById<CardView>(R.id.cardGame3).setOnClickListener {
            bukaDeskripsiGame("urut_angka")
        }
    }

    private fun bukaDeskripsiGame(gameKey: String) {
        if (idAnak.isEmpty()) {
            Toast.makeText(
                this,
                "Data profil anak belum terbaca. Silakan pilih profil anak ulang.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val intent = Intent(this, GameDescriptionActivity::class.java)

        intent.putExtra("ID_ANAK", idAnak)
        intent.putExtra("NAMA_ANAK", namaAnak)
        intent.putExtra("USIA_ANAK", usiaAnak)

        intent.putExtra("id_anak", idAnak)
        intent.putExtra("nama_anak", namaAnak)
        intent.putExtra("usia_anak", usiaAnak)

        intent.putExtra("GAME_KEY", gameKey)

        startActivity(intent)
    }

    private data class GameCardViews(
        val cover: ImageView,
        val title: TextView,
        val description: TextView
    ) {
        fun tampilkan(gameInfo: GameInfo) {
            cover.setImageResource(gameInfo.coverRes)
            title.text = gameInfo.title
            description.text = gameInfo.dashboardDescription
        }
    }

}
