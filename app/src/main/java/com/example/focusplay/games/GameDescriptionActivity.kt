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
        when (gameKey) {
            "antar_rumah" -> {
                imgGame.setImageResource(R.drawable.bg_antar_si_domba)

                tvNamaGame.text = "Antar ke Rumah"

                tvTujuanGame.text =
                    "Melatih fokus, ketelitian, koordinasi tangan dan mata, serta kemampuan mencocokkan warna."

                tvCaraBermain.text =
                    "1. Perhatikan domba yang muncul di area permainan.\n" +
                            "2. Seret domba ke rumah yang memiliki warna yang sama.\n" +
                            "3. Jika jawaban benar, skor akan bertambah.\n" +
                            "4. Jika mode adaptif aktif, fase permainan akan naik atau turun sesuai hasil jawaban."
            }

            "pasang_kartu" -> {
                imgGame.setImageResource(R.drawable.bg_antar_si_domba)

                tvNamaGame.text = "Pasang Kartu"

                tvTujuanGame.text =
                    "Melatih daya ingat, konsentrasi, dan kemampuan mencocokkan pasangan gambar."

                tvCaraBermain.text =
                    "1. Perhatikan kartu yang tersedia.\n" +
                            "2. Pilih kartu yang memiliki pasangan yang sesuai.\n" +
                            "3. Cocokkan kartu dengan teliti.\n" +
                            "4. Kumpulkan skor sebanyak mungkin sebelum waktu habis."
            }

            "urut_angka" -> {
                imgGame.setImageResource(R.drawable.bg_antar_si_domba)

                tvNamaGame.text = "Urutkan Angka"

                tvTujuanGame.text =
                    "Melatih fokus, logika berpikir, dan kemampuan mengenali urutan angka."

                tvCaraBermain.text =
                    "1. Perhatikan angka yang muncul di layar.\n" +
                            "2. Susun angka dari urutan yang benar.\n" +
                            "3. Pilih dengan hati-hati agar skor bertambah.\n" +
                            "4. Selesaikan permainan sebelum waktu habis."
            }

            else -> {
                imgGame.setImageResource(R.drawable.bg_antar_si_domba)

                tvNamaGame.text = "Antar ke Rumah"

                tvTujuanGame.text =
                    "Melatih fokus, ketelitian, koordinasi tangan dan mata, serta kemampuan mencocokkan warna."

                tvCaraBermain.text =
                    "1. Perhatikan domba yang muncul di area permainan.\n" +
                            "2. Seret domba ke rumah yang memiliki warna yang sama.\n" +
                            "3. Jika jawaban benar, skor akan bertambah.\n" +
                            "4. Jika mode adaptif aktif, fase permainan akan naik atau turun sesuai hasil jawaban."
            }
        }
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
            else -> GameAntarRumahActivity::class.java
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