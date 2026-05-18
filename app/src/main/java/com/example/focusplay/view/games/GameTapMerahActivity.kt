package com.example.focusplay.view.games

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import kotlin.random.Random

class GameTapMerahActivity : AppCompatActivity() {

    private lateinit var arenaGame: FrameLayout
    private lateinit var tvSkor: TextView
    private lateinit var tvFase: TextView

    private var skor = 0
    private var faseSaatIni = 1
    private var idAnak = ""

    // Pengatur Waktu untuk target bergerak di Fase 2 & 3
    private val handlerGerak = Handler(Looper.getMainLooper())
    private var delayGerak: Long = 2000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_tap_merah)

        idAnak = intent.getStringExtra("ID_ANAK") ?: ""

        arenaGame = findViewById(R.id.arenaGame)
        tvSkor = findViewById(R.id.tvSkor)
        tvFase = findViewById(R.id.tvFase)

        findViewById<ImageView>(R.id.btnKembali).setOnClickListener {
            finish()
        }

        // Tunggu sampai arena selesai digambar di layar, baru mulai gamenya
        arenaGame.post {
            mulaiRonde()
        }
    }

    private fun mulaiRonde() {
        arenaGame.removeAllViews() // Bersihkan arena

        // Atur parameter berdasarkan fase
        val ukuranTargetDp: Int
        val jumlahPengecoh: Int
        val warnaPengecoh: List<String>

        when (faseSaatIni) {
            1 -> {
                tvFase.text = "Fase 1: Pengenalan"
                ukuranTargetDp = 120
                jumlahPengecoh = 2
                // Pengecoh sangat beda warnanya
                warnaPengecoh = listOf("#2196F3", "#4CAF50", "#FFEB3B")
                handlerGerak.removeCallbacks(runnableGerak) // Diam
            }
            2 -> {
                tvFase.text = "Fase 2: Target Bergerak"
                ukuranTargetDp = 90
                jumlahPengecoh = 4
                warnaPengecoh = listOf("#2196F3", "#4CAF50", "#FFEB3B", "#9C27B0")
                delayGerak = 1200L
                mulaiPergerakanTarget()
            }
            else -> {
                tvFase.text = "Fase 3: Uji Fokus Ekstra!"
                ukuranTargetDp = 70
                jumlahPengecoh = 6
                // Pengecoh mirip dengan warna target (Merah Tua, Oranye, Pink)
                warnaPengecoh = listOf("#B71C1C", "#FF9800", "#E91E63", "#FF5722", "#FFC107")
                delayGerak = 800L
                mulaiPergerakanTarget()
            }
        }

        // 1. Buat Pengecoh (Distraktor)
        for (i in 0 until jumlahPengecoh) {
            val warnaRandom = warnaPengecoh[Random.nextInt(warnaPengecoh.size)]
            buatLingkaran(warnaRandom, ukuranTargetDp, isTarget = false)
        }

        // 2. Buat Target (Merah)
        buatLingkaran("#F44336", ukuranTargetDp, isTarget = true)
    }

    private fun buatLingkaran(warnaHex: String, ukuranDp: Int, isTarget: Boolean) {
        val ukuranPx = (ukuranDp * resources.displayMetrics.density).toInt()

        val view = View(this)
        view.layoutParams = FrameLayout.LayoutParams(ukuranPx, ukuranPx)

        // Membuat bentuk bulat berwarna
        val bentukBulat = GradientDrawable()
        bentukBulat.shape = GradientDrawable.OVAL
        bentukBulat.setColor(Color.parseColor(warnaHex))
        view.background = bentukBulat

        // Menentukan posisi acak di dalam arena
        val maxX = arenaGame.width - ukuranPx
        val maxY = arenaGame.height - ukuranPx
        view.x = Random.nextInt(0, maxX).toFloat()
        view.y = Random.nextInt(0, maxY).toFloat()

        if (isTarget) {
            view.tag = "TARGET"
            view.elevation = 4f // Target selalu di atas pengecoh
            view.setOnClickListener {
                targetBerhasilDitekan()
            }
        } else {
            view.tag = "PENGECOH"
            view.elevation = 2f
            view.setOnClickListener {
                Toast.makeText(this, "Itu bukan merah!", Toast.LENGTH_SHORT).show()
                // Opsional: kurangi skor jika salah tekan
            }
        }

        arenaGame.addView(view)
    }

    private fun targetBerhasilDitekan() {
        skor += 10
        tvSkor.text = "Skor: $skor"

        // Logika naik level
        if (skor == 50 && faseSaatIni == 1) {
            faseSaatIni = 2
            Toast.makeText(this, "Hebat! Lanjut ke Fase 2", Toast.LENGTH_SHORT).show()
        } else if (skor == 100 && faseSaatIni == 2) {
            faseSaatIni = 3
            Toast.makeText(this, "Luar Biasa! Lanjut ke Fase 3", Toast.LENGTH_SHORT).show()
        } else if (skor == 150) {
            Toast.makeText(this, "Kamu berhasil menyelesaikan permainan!", Toast.LENGTH_LONG).show()
            finish() // Mengakhiri permainan
            return
        }

        mulaiRonde() // Refresh arena dengan posisi baru
    }

    // --- LOGIKA PERGERAKAN UNTUK FASE 2 & 3 ---
    private val runnableGerak = object : Runnable {
        override fun run() {
            // Acak posisi semua objek di arena
            for (i in 0 until arenaGame.childCount) {
                val view = arenaGame.getChildAt(i)
                val ukuranPx = view.width
                val maxX = arenaGame.width - ukuranPx
                val maxY = arenaGame.height - ukuranPx

                // Mencegah error jika arena belum siap
                if (maxX > 0 && maxY > 0) {
                    view.animate()
                        .x(Random.nextInt(0, maxX).toFloat())
                        .y(Random.nextInt(0, maxY).toFloat())
                        .setDuration(delayGerak / 2)
                        .start()
                }
            }
            handlerGerak.postDelayed(this, delayGerak)
        }
    }

    private fun mulaiPergerakanTarget() {
        handlerGerak.removeCallbacks(runnableGerak)
        handlerGerak.postDelayed(runnableGerak, delayGerak)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Wajib menghentikan timer saat keluar game agar tidak error / memori bocor
        handlerGerak.removeCallbacks(runnableGerak)
    }
}