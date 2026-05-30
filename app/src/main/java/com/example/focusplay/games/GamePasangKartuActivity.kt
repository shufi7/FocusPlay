package com.example.focusplay.games

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import com.example.focusplay.history.EvaluasiActivity
import com.example.focusplay.utils.AdaptiveGameManager
import com.example.focusplay.utils.GameResultHelper

class GamePasangKartuActivity : AppCompatActivity() {

    private lateinit var gridKartu: GridLayout
    private lateinit var tvSkor: TextView
    private lateinit var tvFase: TextView
    private lateinit var tvTimer: TextView
    private lateinit var adaptiveManager: AdaptiveGameManager

    private var skor = 0
    private var faseSaatIni = 1
    private var idAnak = ""
    private var namaAnak = "Anak"

    private var modeAdaptif = true
    private var targetWaktuMenit = 1

    private var totalBenar = 0
    private var totalSalah = 0
    private var waktuMulaiSesi = 0L
    private var sesiSelesai = false

    private var jumlahPasanganSelesai = 0
    private var totalPasanganRondeIni = 0
    private var rondeAdaSalah = false

    private var kartuPertama: TextView? = null
    private var kartuKedua: TextView? = null
    private var sedangMemeriksa = false

    private var timerPermainan: CountDownTimer? = null
    private val handler = Handler(Looper.getMainLooper())

    private val namaGame = "Pasang Kartu"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_pasang_kartu)

        ambilDataAnakDariIntent()
        hubungkanView()
        bacaPengaturan()
        aturTombol()

        waktuMulaiSesi = System.currentTimeMillis()

        mulaiTimerGlobal()
        mulaiRonde()
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
        gridKartu = findViewById(R.id.gridKartu)
        tvSkor = findViewById(R.id.tvSkor)
        tvFase = findViewById(R.id.tvFase)
        tvTimer = findViewById(R.id.tvTimer)
    }

    private fun bacaPengaturan() {
        val prefs = getSharedPreferences("pengaturan_permainan", MODE_PRIVATE)

        modeAdaptif = prefs.getBoolean("mode_adaptif", true)
        targetWaktuMenit = prefs.getString("target_waktu", "1")?.toIntOrNull() ?: 1

        // Semua game selalu mulai dari fase 1.
        faseSaatIni = 1

        adaptiveManager = AdaptiveGameManager(
            faseSekarang = faseSaatIni,
            modeAdaptifAktif = modeAdaptif
        )
    }

    private fun aturTombol() {
        findViewById<ImageView>(R.id.btnKembali).setOnClickListener {
            timerPermainan?.cancel()
            handler.removeCallbacksAndMessages(null)
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

        gridKartu.removeAllViews()
        jumlahPasanganSelesai = 0
        rondeAdaSalah = false
        kartuPertama = null
        kartuKedua = null
        sedangMemeriksa = false

        val isiKartu = mutableListOf<String>()
        val waktuPreview: Long

        when (faseSaatIni) {
            1 -> {
                tvFase.text = "Fase 1: Pengenalan"
                gridKartu.columnCount = 2
                gridKartu.rowCount = 2
                totalPasanganRondeIni = 2
                waktuPreview = 3000L
                isiKartu.addAll(listOf("🍎", "🍎", "🐶", "🐶"))
            }

            2 -> {
                tvFase.text = "Fase 2: Memori Bertambah"
                gridKartu.columnCount = 3
                gridKartu.rowCount = 3
                totalPasanganRondeIni = 4
                waktuPreview = 2000L
                isiKartu.addAll(listOf("🍎", "🍎", "🐶", "🐶", "🚗", "🚗", "⚽", "⚽"))
            }

            else -> {
                tvFase.text = "Fase 3: Tantangan Mirip"
                gridKartu.columnCount = 3
                gridKartu.rowCount = 4
                totalPasanganRondeIni = 6
                waktuPreview = 1000L
                isiKartu.addAll(
                    listOf(
                        "🍎", "🍎",
                        "🍅", "🍅",
                        "🐶", "🐶",
                        "🐺", "🐺",
                        "🚗", "🚗",
                        "🚙", "🚙"
                    )
                )
            }
        }

        isiKartu.shuffle()

        for (i in 0 until totalPasanganRondeIni * 2) {
            val ukuranKartuPx = dpToPx(80)

            val kartu = TextView(this).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = ukuranKartuPx
                    height = ukuranKartuPx
                    setMargins(12, 12, 12, 12)
                }

                textSize = 36f
                gravity = Gravity.CENTER
                elevation = 4f
                tag = isiKartu[i]

                text = isiKartu[i]
                background = buatBackgroundKartu("#FFFFFF", "#E0E0E0")
            }

            gridKartu.addView(kartu)
        }

        handler.postDelayed({
            if (sesiSelesai) return@postDelayed

            for (i in 0 until gridKartu.childCount) {
                val kartu = gridKartu.getChildAt(i) as TextView
                kartu.text = "?"
                kartu.typeface = Typeface.DEFAULT_BOLD
                kartu.setTextColor(Color.parseColor("#334155"))
                kartu.background = buatBackgroundKartu("#EAF7FF", "#B9DDF6")

                kartu.setOnClickListener {
                    pilihKartu(kartu)
                }
            }
        }, waktuPreview)
    }

    private fun pilihKartu(kartu: TextView) {
        if (sesiSelesai || sedangMemeriksa) return
        if (kartu == kartuPertama) return
        if (kartu.text != "?") return

        kartu.text = kartu.tag.toString()
        kartu.setTextColor(Color.parseColor("#111111"))
        kartu.background = buatBackgroundKartu("#FFFFFF", "#8DB52A")

        if (kartuPertama == null) {
            kartuPertama = kartu
        } else {
            kartuKedua = kartu
            cekPasangan()
        }
    }

    private fun cekPasangan() {
        sedangMemeriksa = true

        val sama = kartuPertama?.tag == kartuKedua?.tag

        if (sama) {
            kartuPertama?.setBackgroundColor(Color.parseColor("#C8E6C9"))
            kartuKedua?.setBackgroundColor(Color.parseColor("#C8E6C9"))

            kartuPertama?.setOnClickListener(null)
            kartuKedua?.setOnClickListener(null)

            jumlahPasanganSelesai++
            skor += 15
            totalBenar++

            tvSkor.text = "Skor: $skor"

            handler.postDelayed({
                if (sesiSelesai) return@postDelayed

                resetPilihan()

                if (jumlahPasanganSelesai == totalPasanganRondeIni) {
                    prosesAdaptifSetelahRondeSelesai()
                }
            }, 500)

        } else {
            totalSalah++
            rondeAdaSalah = true

            handler.postDelayed({
                if (sesiSelesai) return@postDelayed

                kartuPertama?.text = "?"
                kartuKedua?.text = "?"
                kartuPertama?.setTextColor(Color.parseColor("#334155"))
                kartuKedua?.setTextColor(Color.parseColor("#334155"))
                kartuPertama?.background = buatBackgroundKartu("#EAF7FF", "#B9DDF6")
                kartuKedua?.background = buatBackgroundKartu("#EAF7FF", "#B9DDF6")

                resetPilihan()
            }, 700)
        }
    }

    private fun prosesAdaptifSetelahRondeSelesai() {
        /*
            Mode adaptif dihitung per ronde, bukan per pasangan kartu.

            Jika 1 ronde selesai tanpa salah pasangan:
            - dihitung sebagai 1 jawaban benar untuk AdaptiveGameManager.

            Jika dalam 1 ronde pernah salah memasangkan kartu:
            - dihitung sebagai 1 jawaban salah untuk AdaptiveGameManager.
        */
        val rondeSempurna = !rondeAdaSalah
        val faseBaru = adaptiveManager.prosesJawaban(rondeSempurna)

        if (faseBaru != faseSaatIni) {
            faseSaatIni = faseBaru
            Toast.makeText(this, "Fase berubah ke Fase $faseSaatIni", Toast.LENGTH_SHORT).show()
        }

        mulaiRonde()
    }

    private fun resetPilihan() {
        kartuPertama = null
        kartuKedua = null
        sedangMemeriksa = false
    }

    private fun simpanRiwayatAkhir() {
        if (sesiSelesai) return
        sesiSelesai = true

        timerPermainan?.cancel()
        handler.removeCallbacksAndMessages(null)

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

    private fun buatBackgroundKartu(warnaIsi: String, warnaStroke: String): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dpToPx(18).toFloat()
            setColor(Color.parseColor(warnaIsi))
            setStroke(dpToPx(2), Color.parseColor(warnaStroke))
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
        timerPermainan?.cancel()
        handler.removeCallbacksAndMessages(null)
    }
}
