package com.example.focusplay.view.games

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.CountDownTimer
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

class GameTangkapWarnaActivity : AppCompatActivity() {

    private lateinit var arenaGame: FrameLayout
    private lateinit var tvSkor: TextView
    private lateinit var tvFase: TextView
    private lateinit var tvTimer: TextView
    private lateinit var tvTargetWarna: TextView

    private var skor = 0
    private var faseSaatIni = 1
    private var idAnak = ""

    private var namaWarnaTarget = ""
    private var timerFase3: CountDownTimer? = null
    private var waktuHabis = false

    // Handler untuk membuat objek melayang di Fase 3
    private val handlerGerak = Handler(Looper.getMainLooper())
    private val delayGerak: Long = 1000L

    // Kamus Warna Utama
    private val kamusWarna = mapOf(
        "MERAH" to "#F44336",
        "BIRU" to "#2196F3",
        "HIJAU" to "#4CAF50",
        "KUNING" to "#FFEB3B",
        "UNGU" to "#9C27B0",
        "ORANYE" to "#FF9800",
        "MERAH MUDA" to "#E91E63",
        "COKELAT" to "#795548"
    )

    // Kamus Warna Pengecoh (Mirip) untuk Fase 3
    private val warnaPengecohMirip = listOf("#D32F2F", "#1976D2", "#388E3C", "#FBC02D", "#7B1FA2")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_tangkap_warna)

        idAnak = intent.getStringExtra("ID_ANAK") ?: ""

        arenaGame = findViewById(R.id.arenaGame)
        tvSkor = findViewById(R.id.tvSkor)
        tvFase = findViewById(R.id.tvFase)
        tvTimer = findViewById(R.id.tvTimer)
        tvTargetWarna = findViewById(R.id.tvTargetWarna)

        // Bikin latar spanduknya melengkung sedikit
        val papanTarget = tvTargetWarna.parent as View
        val bgPapan = GradientDrawable()
        bgPapan.shape = GradientDrawable.RECTANGLE
        bgPapan.cornerRadius = dpToPx(12).toFloat()
        bgPapan.setColor(Color.parseColor("#6A1B9A")) // Ungu
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
        handlerGerak.removeCallbacks(runnableGerak)
        waktuHabis = false

        val jumlahObjek: Int
        var pakaiPengecohMirip = false

        when (faseSaatIni) {
            1 -> {
                tvFase.text = "Fase 1: Mengenal Warna"
                tvTimer.visibility = View.GONE
                jumlahObjek = 3 // 1 Target + 2 Warna Beda
            }
            2 -> {
                tvFase.text = "Fase 2: Semakin Ramai"
                tvTimer.visibility = View.GONE
                jumlahObjek = 5 // 1 Target + 4 Warna Beda
            }
            else -> {
                tvFase.text = "Fase 3: Tangkap yang Bergerak!"
                tvTimer.visibility = View.VISIBLE
                jumlahObjek = 7
                pakaiPengecohMirip = true
                mulaiTimer(25000) // 25 Detik
                mulaiPergerakanTarget()
            }
        }

        // Tentukan Target Warna Ronde Ini
        val namaWarnaAcak = kamusWarna.keys.toList().shuffled()
        namaWarnaTarget = namaWarnaAcak[0]
        tvTargetWarna.text = namaWarnaTarget

        val hexTarget = kamusWarna[namaWarnaTarget]!!
        val warnaTampil = mutableListOf(hexTarget)

        // Isi sisa objek dengan warna lain
        for (i in 1 until jumlahObjek) {
            if (pakaiPengecohMirip && i % 2 == 0) {
                // Di Fase 3, campur dengan warna pengecoh yang membingungkan
                warnaTampil.add(warnaPengecohMirip.random())
            } else {
                warnaTampil.add(kamusWarna[namaWarnaAcak[i]]!!)
            }
        }

        warnaTampil.shuffle() // Acak letak target biar gak selalu di awal

        val maxX = arenaGame.width - dpToPx(80)
        val maxY = arenaGame.height - dpToPx(80)

        // Sebarkan Bola Warna
        for (warnaHex in warnaTampil) {
            val isTarget = (warnaHex == hexTarget)
            buatBolaWarna(warnaHex, maxX, maxY, isTarget)
        }
    }

    private fun buatBolaWarna(warnaHex: String, maxX: Int, maxY: Int, isTarget: Boolean) {
        val ukuranPx = dpToPx(80)
        val bola = View(this).apply {
            layoutParams = FrameLayout.LayoutParams(ukuranPx, ukuranPx)
            elevation = 4f

            val bulatBg = GradientDrawable()
            bulatBg.shape = GradientDrawable.OVAL
            bulatBg.setColor(Color.parseColor(warnaHex))
            background = bulatBg

            if (maxX > 0 && maxY > 0) {
                x = Random.nextInt(0, maxX).toFloat()
                y = Random.nextInt(0, maxY).toFloat()
            }

            setOnClickListener {
                if (waktuHabis) return@setOnClickListener

                if (isTarget) {
                    skor += 10
                    tvSkor.text = "Skor: $skor"
                    cekNaikFase()
                } else {
                    Toast.makeText(this@GameTangkapWarnaActivity, "Ups, salah warna!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        arenaGame.addView(bola)
    }

    private val runnableGerak = object : Runnable {
        override fun run() {
            for (i in 0 until arenaGame.childCount) {
                val view = arenaGame.getChildAt(i)
                val maxX = arenaGame.width - view.width
                val maxY = arenaGame.height - view.height

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
        handlerGerak.postDelayed(runnableGerak, delayGerak)
    }

    private fun cekNaikFase() {
        if (skor == 50 && faseSaatIni == 1) {
            faseSaatIni = 2
            Toast.makeText(this, "Mata yang jeli! Lanjut Fase 2", Toast.LENGTH_SHORT).show()
        } else if (skor == 100 && faseSaatIni == 2) {
            faseSaatIni = 3
            Toast.makeText(this, "Awas, target mulai melayang!", Toast.LENGTH_SHORT).show()
        } else if (skor >= 150) {
            Toast.makeText(this, "Luar Biasa! Kamu ahli warna!", Toast.LENGTH_LONG).show()
            timerFase3?.cancel()
            simpanRiwayatAkhir("Tangkap Warna")
            finish()
            return
        }
        mulaiRonde()
    }

    private fun mulaiTimer(durasiMillis: Long) {
        timerFase3?.cancel()
        timerFase3 = object : CountDownTimer(durasiMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val detik = millisUntilFinished / 1000
                tvTimer.text = "⏳ ${detik}s"
            }

            override fun onFinish() {
                waktuHabis = true
                tvTimer.text = "Habis!"
                Toast.makeText(this@GameTangkapWarnaActivity, "Waktu Habis! Kita ulang ronde ini.", Toast.LENGTH_SHORT).show()
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
        handlerGerak.removeCallbacks(runnableGerak)
    }

    private fun simpanRiwayatAkhir(namaGame: String) {
        val nama = intent.getStringExtra("NAMA_ANAK") ?: "Anak"
        val idAnak = intent.getStringExtra("ID_ANAK") ?: ""
        val akurasiSimulasi = if (skor >= 100) 100 else 80

        com.example.focusplay.utils.GameResultHelper.evaluasiDanSimpanRealtime(
            activity = this,
            idAnak = idAnak,
            namaAnak = nama,
            namaGame = namaGame,
            skor = skor,
            akurasi = akurasiSimulasi,
            durasiMenit = 2,
            onSelesai = { hasilEvaluasi ->

                // Melompat ke halaman Evaluasi dengan membawa hasil AI
                val intentToEvaluasi = android.content.Intent(this, com.example.focusplay.view.EvaluasiActivity::class.java)
                intentToEvaluasi.putExtra("ID_ANAK", idAnak)
                intentToEvaluasi.putExtra("NAMA_ANAK", nama)
                intentToEvaluasi.putExtra("EVALUASI_LANGSUNG", hasilEvaluasi)
                startActivity(intentToEvaluasi)

                // Menutup layar game setelah pindah
                finish()
            }
        )
    }
}