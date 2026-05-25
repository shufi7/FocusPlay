package com.example.focusplay.games

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import com.example.focusplay.history.EvaluasiActivity
import com.example.focusplay.utils.GameResultHelper
import kotlin.random.Random

class GameUrutkanAngkaActivity : AppCompatActivity() {

    private lateinit var arenaGame: FrameLayout
    private lateinit var tvSkor: TextView
    private lateinit var tvFase: TextView
    private lateinit var tvTimer: TextView
    private lateinit var tvTargetAngka: TextView

    private var skor = 0
    private var faseSaatIni = 1
    private var idAnak = ""

    private var angkaSelanjutnya = 1
    private var targetMaksimal = 3

    private var timerFase3: CountDownTimer? = null
    private var waktuHabis = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_urutkan_angka)

        idAnak = intent.getStringExtra("ID_ANAK") ?: ""

        arenaGame = findViewById(R.id.arenaGame)
        tvSkor = findViewById(R.id.tvSkor)
        tvFase = findViewById(R.id.tvFase)
        tvTimer = findViewById(R.id.tvTimer)
        tvTargetAngka = findViewById(R.id.tvTargetAngka)

        // Ubah warna latar belakang papan target secara dinamis
        val papanTarget = tvTargetAngka.parent as View
        val bgPapan = GradientDrawable()
        bgPapan.shape = GradientDrawable.RECTANGLE
        bgPapan.cornerRadius = dpToPx(12).toFloat()
        bgPapan.setColor(Color.parseColor("#FF9800")) // Warna Oranye
        papanTarget.background = bgPapan

        findViewById<ImageView>(R.id.btnKembali).setOnClickListener {
            finish()
        }

        arenaGame.post {
            mulaiRonde()
        }
    }

    private fun mulaiRonde() {
        arenaGame.removeAllViews()
        angkaSelanjutnya = 1
        waktuHabis = false
        timerFase3?.cancel()
        updatePapanTarget()

        val listAngkaTampil = mutableListOf<Int>()

        when (faseSaatIni) {
            1 -> {
                tvFase.text = "Fase 1: Berhitung Dasar"
                tvTimer.visibility = View.GONE
                targetMaksimal = 3 // Urutkan 1, 2, 3
                listAngkaTampil.addAll(listOf(1, 2, 3))
            }
            2 -> {
                tvFase.text = "Fase 2: Urutan Panjang"
                tvTimer.visibility = View.GONE
                targetMaksimal = 5 // Urutkan 1, 2, 3, 4, 5
                listAngkaTampil.addAll(listOf(1, 2, 3, 4, 5))
            }
            else -> {
                tvFase.text = "Fase 3: Awas Pengecoh!"
                tvTimer.visibility = View.VISIBLE
                targetMaksimal = 5

                // Tambahkan angka target
                listAngkaTampil.addAll(listOf(1, 2, 3, 4, 5))
                // Tambahkan angka pengecoh (distraktor)
                listAngkaTampil.addAll(listOf(7, 9))

                mulaiTimer(20000) // Waktu: 20 detik
            }
        }

        // Acak urutan pembuatan agar posisinya random
        listAngkaTampil.shuffle()

        val maxX = arenaGame.width - dpToPx(70)
        val maxY = arenaGame.height - dpToPx(70)

        // Sebarkan bola-bola angka di dalam arena
        for (angka in listAngkaTampil) {
            val isTarget = angka <= targetMaksimal
            buatBolaAngka(angka, maxX, maxY, isTarget)
        }
    }

    private fun buatBolaAngka(angka: Int, maxX: Int, maxY: Int, isTarget: Boolean) {
        val ukuranPx = dpToPx(70)

        val bola = TextView(this).apply {
            layoutParams = FrameLayout.LayoutParams(ukuranPx, ukuranPx)
            text = angka.toString()
            textSize = 28f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            elevation = 4f

            val bulatBg = GradientDrawable()
            bulatBg.shape = GradientDrawable.OVAL

            // Warnanya beda-beda agar menarik untuk anak
            val warnaBg = when (angka % 4) {
                0 -> "#E91E63" // Pink
                1 -> "#2196F3" // Biru
                2 -> "#4CAF50" // Hijau
                else -> "#9C27B0" // Ungu
            }
            bulatBg.setColor(Color.parseColor(warnaBg))
            background = bulatBg

            // Posisi Acak
            if (maxX > 0 && maxY > 0) {
                x = Random.nextInt(0, maxX).toFloat()
                y = Random.nextInt(0, maxY).toFloat()
            }

            setOnClickListener {
                if (waktuHabis) return@setOnClickListener

                if (!isTarget) {
                    Toast.makeText(this@GameUrutkanAngkaActivity, "Itu angka pengecoh!", Toast.LENGTH_SHORT).show()
                } else if (angka == angkaSelanjutnya) {
                    // Ketukan Benar!
                    this.visibility = View.GONE // Hilangkan bola
                    angkaSelanjutnya++
                    skor += 10
                    tvSkor.text = "Skor: $skor"

                    if (angkaSelanjutnya > targetMaksimal) {
                        cekNaikFase()
                    } else {
                        updatePapanTarget()
                    }
                } else {
                    // Salah urutan
                    Toast.makeText(this@GameUrutkanAngkaActivity, "Cari angka $angkaSelanjutnya dulu ya!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        arenaGame.addView(bola)
    }

    private fun updatePapanTarget() {
        tvTargetAngka.text = angkaSelanjutnya.toString()
    }

    private fun cekNaikFase() {
        if (faseSaatIni == 1) {
            faseSaatIni = 2
            Toast.makeText(this, "Hebat! Lanjut ke Fase 2", Toast.LENGTH_SHORT).show()
        } else if (faseSaatIni == 2) {
            faseSaatIni = 3
            Toast.makeText(this, "Luar biasa! Hati-hati jebakan di Fase 3", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Sempurna! Kamu juara berhitung!", Toast.LENGTH_LONG).show()
            timerFase3?.cancel()
            simpanRiwayatAkhir("Urutkan Angka")
            finish()
            return
        }
        mulaiRonde()
    }

    private fun mulaiTimer(durasiMillis: Long) {
        timerFase3 = object : CountDownTimer(durasiMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val detik = millisUntilFinished / 1000
                tvTimer.text = "⏳ ${detik}s"
            }

            override fun onFinish() {
                waktuHabis = true
                tvTimer.text = "Habis!"
                Toast.makeText(this@GameUrutkanAngkaActivity, "Waktu Habis! Kita ulang ronde ini ya.", Toast.LENGTH_SHORT).show()
                mulaiRonde()
            }
        }.start()
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
        timerFase3?.cancel()
    }

    private fun simpanRiwayatAkhir(namaGame: String) {
        val nama = intent.getStringExtra("NAMA_ANAK") ?: "Anak"
        val idAnak = intent.getStringExtra("ID_ANAK") ?: ""
        val akurasiSimulasi = if (skor >= 100) 100 else 80

        GameResultHelper.evaluasiDanSimpanRealtime(
            activity = this,
            idAnak = idAnak,
            namaAnak = nama,
            namaGame = namaGame,
            skor = skor,
            akurasi = akurasiSimulasi,
            durasiMenit = 2,
            onSelesai = { hasilEvaluasi ->
                // LOMPAT KE HALAMAN EVALUASI
                val intentToEvaluasi = Intent(this, EvaluasiActivity::class.java)
                intentToEvaluasi.putExtra("ID_ANAK", idAnak)
                intentToEvaluasi.putExtra("NAMA_ANAK", nama)
                intentToEvaluasi.putExtra("EVALUASI_LANGSUNG", hasilEvaluasi)
                startActivity(intentToEvaluasi)

                // Tutup game setelah melompat
                finish()
            }
        )
        // 🚨 SANGAT PENTING: JANGAN ADA TULISAN finish() SAMA SEKALI DI AREA SINI!
    }
}