package com.example.focusplay.games

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R

class GameDescriptionActivity : AppCompatActivity() {

    private var idAnak: String = ""
    private var namaAnak: String = "Anak"
    private var usiaAnak: Int = 0
    private var gameKey: String = "antar_rumah"

    private lateinit var btnBack: ImageView
    private lateinit var imgGame: ImageView
    private lateinit var tvNamaGame: TextView
    private lateinit var tvTujuanGame: TextView
    private lateinit var tvCaraBermain: TextView
    private lateinit var btnMulaiGame: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_description)

        ambilDataIntent()
        hubungkanView()
        tampilkanDeskripsiGame()
        aturTombol()
    }

    private fun ambilDataIntent() {
        idAnak = intent.getStringExtra("ID_ANAK")
            ?: intent.getStringExtra("id_anak")
                    ?: ""

        namaAnak = intent.getStringExtra("NAMA_ANAK")
            ?: intent.getStringExtra("nama_anak")
                    ?: "Anak"

        usiaAnak = intent.getIntExtra(
            "USIA_ANAK",
            intent.getIntExtra("usia_anak", 0)
        )

        gameKey = intent.getStringExtra("GAME_KEY") ?: "antar_rumah"
    }

    private fun hubungkanView() {
        btnBack = findViewById(R.id.btnBack)
        imgGame = findViewById(R.id.imgGame)
        tvNamaGame = findViewById(R.id.tvNamaGame)
        tvTujuanGame = findViewById(R.id.tvTujuanGame)
        tvCaraBermain = findViewById(R.id.tvCaraBermain)
        btnMulaiGame = findViewById(R.id.btnMulaiGame)
    }

    private fun tampilkanDeskripsiGame() {
        val gameInfo = GameCatalog.get(gameKey)
        imgGame.setImageResource(gameInfo.coverRes)
        tvNamaGame.text = gameInfo.title
        tvTujuanGame.text = gameInfo.goal
        tvCaraBermain.text = gameInfo.howToPlay
    }

    private fun aturTombol() {
        btnBack.jadiTombolCepat {
            finish()
        }

        btnMulaiGame.jadiTombolCepat {
            bukaGame()
        }
    }

    private fun bukaGame() {
        val targetActivity = when (gameKey) {
            "pasang_kartu" -> GamePasangKartuActivity::class.java
            "urut_angka" -> GameUrutkanAngkaActivity::class.java
            else -> GameAntarSiDombaActivity::class.java
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

    private fun View.jadiTombolCepat(onClick: () -> Unit) {
        isClickable = true
        isFocusable = true

        setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate()
                        .scaleX(0.94f)
                        .scaleY(0.94f)
                        .setDuration(35)
                        .start()
                    true
                }

                MotionEvent.ACTION_UP -> {
                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(45)
                        .withEndAction {
                            onClick()
                        }
                        .start()
                    true
                }

                MotionEvent.ACTION_CANCEL -> {
                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(45)
                        .start()
                    true
                }

                else -> true
            }
        }
    }
}
