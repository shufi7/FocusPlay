package com.example.focusplay.view.games

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R

class GameTangkapWarnaActivity : AppCompatActivity() {

    private lateinit var tvSkor: TextView
    private lateinit var tvWaktu: TextView
    private lateinit var tvTargetWarna: TextView
    private lateinit var gridLayoutWarna: GridLayout

    private var skor = 0
    private var timer: CountDownTimer? = null
    private var isGameRunning = false
    private var warnaTargetSaatIni: String = ""

    // Kamus Warna yang akan digunakan di permainan
    private val kamusWarna = mapOf(
        "MERAH" to "#F44336",
        "BIRU" to "#2196F3",
        "HIJAU" to "#4CAF50",
        "KUNING" to "#FFEB3B",
        "UNGU" to "#9C27B0",
        "ORANYE" to "#FF9800",
        "COKELAT" to "#795548",
        "HITAM" to "#212121"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_tangkap_warna)

        tvSkor = findViewById(R.id.tvSkor)
        tvWaktu = findViewById(R.id.tvWaktu)
        tvTargetWarna = findViewById(R.id.tvTargetWarna)
        gridLayoutWarna = findViewById(R.id.gridLayoutWarna)

        mulaiPermainan()
    }

    private fun mulaiPermainan() {
        skor = 0
        tvSkor.text = "Skor: 0"
        isGameRunning = true

        buatSoalBaru()

        timer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tvWaktu.text = "Sisa: ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                isGameRunning = false
                tvWaktu.text = "Waktu Habis!"
                gridLayoutWarna.visibility = View.GONE
                tvTargetWarna.text = "Selesai!"

                Toast.makeText(this@GameTangkapWarnaActivity, "Hebat! Total skormu: $skor", Toast.LENGTH_LONG).show()
            }
        }.start()
    }

    private fun buatSoalBaru() {
        // Bersihkan kotak warna sebelumnya (jika ada)
        gridLayoutWarna.removeAllViews()

        // Ambil 4 warna secara acak dari kamus
        val empatWarnaAcak = kamusWarna.keys.shuffled().take(4)

        // Pilih salah satu dari 4 warna tersebut sebagai target
        warnaTargetSaatIni = empatWarnaAcak.random()
        tvTargetWarna.text = warnaTargetSaatIni

        // --- Kalkulasi ukuran kotak (Responsif 2 Kolom) ---
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val totalRuangKosong = (64 * displayMetrics.density).toInt()
        val ukuranKotak = (screenWidth - totalRuangKosong) / 2
        val marginKotak = (8 * displayMetrics.density).toInt()

        // Cetak ke-4 kotak ke layar
        for (namaWarna in empatWarnaAcak) {
            val kotak = View(this)
            val params = GridLayout.LayoutParams()
            params.width = ukuranKotak
            params.height = ukuranKotak
            params.setMargins(marginKotak, marginKotak, marginKotak, marginKotak)
            params.setGravity(Gravity.CENTER)
            kotak.layoutParams = params

            // Warnai kotak sesuai kode HEX di kamus
            val kodeHex = kamusWarna[namaWarna]
            kotak.setBackgroundColor(Color.parseColor(kodeHex))
            kotak.elevation = 6f

            // Aksi saat kotak disentuh
            kotak.setOnClickListener {
                if (isGameRunning) {
                    if (namaWarna == warnaTargetSaatIni) {
                        // BENAR
                        skor++
                        tvSkor.text = "Skor: $skor"
                        buatSoalBaru() // Lanjut ke warna berikutnya
                    } else {
                        // SALAH (Berikan penalti peringatan)
                        Toast.makeText(this, "Itu bukan warna $warnaTargetSaatIni!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            gridLayoutWarna.addView(kotak)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}