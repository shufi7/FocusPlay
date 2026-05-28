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
import com.example.focusplay.utils.AdaptiveGameManager
import com.example.focusplay.utils.GameResultHelper
import kotlin.random.Random

class GameUrutkanAngkaActivity : AppCompatActivity() {

    private lateinit var arenaGame: FrameLayout
    private lateinit var tvSkor: TextView
    private lateinit var tvFase: TextView
    private lateinit var tvTimer: TextView
    private lateinit var tvTargetAngka: TextView
    private lateinit var adaptiveManager: AdaptiveGameManager

    private var skor = 0
    private var faseSaatIni = 1
    private var idAnak = ""
    private var namaAnak = "Anak"

    private var angkaSelanjutnya = 1
    private var targetMaksimal = 3

    private var modeAdaptif = true
    private var targetWaktuMenit = 1

    private var totalBenar = 0
    private var totalSalah = 0
    private var waktuMulaiSesi = 0L
    private var sesiSelesai = false

    private var timerPermainan: CountDownTimer? = null

    private val namaGame = "Urut Angka"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_urutkan_angka)

        ambilDataAnakDariIntent()
        hubungkanView()
        aturPapanTarget()
        bacaPengaturan()
        aturTombol()

        waktuMulaiSesi = System.currentTimeMillis()

        mulaiTimerGlobal()

        arenaGame.post {
            mulaiRonde()
        }
    }

    private fun ambilDataAnakDariIntent() {
        idAnak = intent.getStringExtra("ID_ANAK")
            ?: intent.getStringExtra("id_anak")
                    ?: ""

        namaAnak = intent.getStringExtra("NAMA_ANAK")
            ?: intent.getStringExtra("nama_anak")
                    ?: "Anak"
    }

    private fun hubungkanView() {
        arenaGame = findViewById(R.id.arenaGame)
        tvSkor = findViewById(R.id.tvSkor)
        tvFase = findViewById(R.id.tvFase)
        tvTimer = findViewById(R.id.tvTimer)
        tvTargetAngka = findViewById(R.id.tvTargetAngka)
    }

    private fun aturPapanTarget() {
        val papanTarget = tvTargetAngka.parent as View
        val bgPapan = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dpToPx(12).toFloat()
            setColor(Color.parseColor("#FF9800"))
        }
        papanTarget.background = bgPapan
    }

    private fun bacaPengaturan() {
        val prefs = getSharedPreferences("pengaturan_permainan", MODE_PRIVATE)

        modeAdaptif = prefs.getBoolean("mode_adaptif", true)
        targetWaktuMenit = prefs.getString("target_waktu", "1")?.toIntOrNull() ?: 1

        faseSaatIni = 1

        adaptiveManager = AdaptiveGameManager(
            faseSekarang = faseSaatIni,
            modeAdaptifAktif = modeAdaptif
        )
    }

    private fun aturTombol() {
        findViewById<ImageView>(R.id.btnKembali).setOnClickListener {
            timerPermainan?.cancel()
            finish()
        }
    }

    private fun mulaiTimerGlobal() {
        val totalMillis = targetWaktuMenit * 60 * 1000L

        tvTimer.visibility = View.VISIBLE

        timerPermainan = object : CountDownTimer(totalMillis, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val detik = millisUntilFinished / 1000
                val menit = detik / 60
                val sisaDetik = detik % 60
                tvTimer.text = "${menit}:${sisaDetik.toString().padStart(2, '0')}"
            }

            override fun onFinish() {
                tvTimer.text = "0:00"
                simpanRiwayatAkhir()
            }
        }.start()
    }

    private fun mulaiRonde() {
        if (sesiSelesai) return

        arenaGame.removeAllViews()
        angkaSelanjutnya = 1
        updatePapanTarget()

        val listAngkaTampil = mutableListOf<Int>()

        when (faseSaatIni) {
            1 -> {
                tvFase.text = "Fase 1: Berhitung Dasar"
                targetMaksimal = 3
                listAngkaTampil.addAll(listOf(1, 2, 3))
            }

            2 -> {
                tvFase.text = "Fase 2: Urutan Panjang"
                targetMaksimal = 5
                listAngkaTampil.addAll(listOf(1, 2, 3, 4, 5))
            }

            else -> {
                tvFase.text = "Fase 3: Awas Pengecoh"
                targetMaksimal = 5
                listAngkaTampil.addAll(listOf(1, 2, 3, 4, 5))
                listAngkaTampil.addAll(listOf(7, 9))
            }
        }

        listAngkaTampil.shuffle()

        val maxX = arenaGame.width - dpToPx(70)
        val maxY = arenaGame.height - dpToPx(70)

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

            val bulatBg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL

                val warnaBg = when (angka % 4) {
                    0 -> "#E91E63"
                    1 -> "#2196F3"
                    2 -> "#4CAF50"
                    else -> "#9C27B0"
                }

                setColor(Color.parseColor(warnaBg))
            }

            background = bulatBg

            if (maxX > 0 && maxY > 0) {
                x = Random.nextInt(0, maxX).toFloat()
                y = Random.nextInt(0, maxY).toFloat()
            }

            setOnClickListener {
                if (sesiSelesai) return@setOnClickListener

                if (!isTarget) {
                    prosesJawabanSalah("Itu angka pengecoh!")
                } else if (angka == angkaSelanjutnya) {
                    prosesJawabanBenar(this)
                } else {
                    prosesJawabanSalah("Cari angka $angkaSelanjutnya dulu ya!")
                }
            }
        }

        arenaGame.addView(bola)
    }

    private fun prosesJawabanBenar(bola: TextView) {
        bola.visibility = View.GONE

        angkaSelanjutnya++
        skor += 10
        totalBenar++

        tvSkor.text = "Skor: $skor"

        if (angkaSelanjutnya > targetMaksimal) {
            val faseBaru = adaptiveManager.prosesJawaban(true)

            if (faseBaru != faseSaatIni) {
                faseSaatIni = faseBaru
                Toast.makeText(this, "Fase berubah ke Fase $faseSaatIni", Toast.LENGTH_SHORT).show()
            }

            mulaiRonde()
        } else {
            updatePapanTarget()
        }
    }

    private fun prosesJawabanSalah(pesan: String) {
        totalSalah++

        Toast.makeText(this, pesan, Toast.LENGTH_SHORT).show()

        val faseBaru = adaptiveManager.prosesJawaban(false)

        if (faseBaru != faseSaatIni) {
            faseSaatIni = faseBaru
            Toast.makeText(this, "Fase berubah ke Fase $faseSaatIni", Toast.LENGTH_SHORT).show()
            mulaiRonde()
        }
    }

    private fun updatePapanTarget() {
        tvTargetAngka.text = angkaSelanjutnya.toString()
    }

    private fun simpanRiwayatAkhir() {
        if (sesiSelesai) return
        sesiSelesai = true

        timerPermainan?.cancel()
        arenaGame.removeAllViews()

        val totalJawaban = totalBenar + totalSalah

        val akurasi = if (totalJawaban > 0) {
            ((totalBenar * 100f) / totalJawaban).toInt()
        } else {
            0
        }

        val durasiMillis = System.currentTimeMillis() - waktuMulaiSesi
        val durasiMenit = maxOf(1, (durasiMillis / 60000L).toInt())

        GameResultHelper.evaluasiDanSimpanRealtime(
            activity = this,
            idAnak = idAnak,
            namaAnak = namaAnak,
            namaGame = namaGame,
            skor = skor,
            akurasi = akurasi,
            durasiMenit = durasiMenit,
            onSelesai = { hasilEvaluasi ->
                val intentToEvaluasi = Intent(this, EvaluasiActivity::class.java)
                intentToEvaluasi.putExtra("ID_ANAK", idAnak)
                intentToEvaluasi.putExtra("NAMA_ANAK", namaAnak)
                intentToEvaluasi.putExtra("EVALUASI_LANGSUNG", hasilEvaluasi)
                startActivity(intentToEvaluasi)
                finish()
            }
        )
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
        timerPermainan?.cancel()
    }
}