package com.example.focusplay.games

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import com.example.focusplay.history.EvaluasiActivity
import com.example.focusplay.utils.AdaptiveGameManager
import com.example.focusplay.utils.GameResultHelper

class GameUrutkanAngkaActivity : AppCompatActivity() {

    private lateinit var arenaGame: FrameLayout
    private lateinit var containerAngka: LinearLayout
    private lateinit var tvSkor: TextView
    private lateinit var tvFase: TextView
    private lateinit var tvTimer: TextView
    private lateinit var tvTargetAngka: TextView
    private lateinit var adaptiveManager: AdaptiveGameManager

    private var skor = 0
    private var faseSaatIni = 1
    private var idAnak = ""
    private var namaAnak = "Anak"
    private var usiaAnak = 0

    private var angkaSelanjutnya = 1
    private var targetMaksimal = 3

    private var modeAdaptif = true
    private var targetWaktuMenit = 1

    private var totalBenar = 0
    private var totalSalah = 0
    private var waktuMulaiSesi = 0L
    private var sesiSelesai = false
    private var sedangTransisiRonde = false
    private var rondeAdaSalah = false
    private var acakRondeBerikutnya = false

    private var timerPermainan: CountDownTimer? = null

    private val namaGame = "Urut Angka"

    private data class ItemAngka(
        val angka: Int,
        val isTarget: Boolean
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_urutkan_angka)

        ambilDataAnakDariIntent()
        hubungkanView()
        bacaPengaturan()
        aturTombol()

        waktuMulaiSesi = System.currentTimeMillis()

        updateHud()
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

        usiaAnak = intent.getIntExtra(
            "USIA_ANAK",
            intent.getIntExtra("usia_anak", 0)
        )
    }

    private fun hubungkanView() {
        arenaGame = findViewById(R.id.arenaGame)
        containerAngka = findViewById(R.id.containerAngka)
        tvSkor = findViewById(R.id.tvSkor)
        tvFase = findViewById(R.id.tvFase)
        tvTimer = findViewById(R.id.tvTimer)
        tvTargetAngka = findViewById(R.id.tvTargetAngka)
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
        val btnKembali = findViewById<ImageView>(R.id.btnKembali)
        btnKembali.setOnClickListener {
            timerPermainan?.cancel()
            finish()
        }

        btnKembali.setOnTouchListener { view, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> view.animate().scaleX(0.94f).scaleY(0.94f).setDuration(45).start()
                android.view.MotionEvent.ACTION_UP,
                android.view.MotionEvent.ACTION_CANCEL -> view.animate().scaleX(1f).scaleY(1f).setDuration(60).start()
            }
            false
        }
    }

    private fun mulaiTimerGlobal() {
        val totalMillis = targetWaktuMenit * 60 * 1000L

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
        if (arenaGame.width <= 0 || arenaGame.height <= 0) {
            arenaGame.post { mulaiRonde() }
            return
        }

        containerAngka.removeAllViews()
        angkaSelanjutnya = 1
        rondeAdaSalah = false
        sedangTransisiRonde = false

        val itemAngkaDasar = buatDaftarAngkaFase()
        val itemAngka = if (acakRondeBerikutnya) {
            itemAngkaDasar.shuffled()
        } else {
            itemAngkaDasar
        }
        updateHud()
        updatePapanTarget()
        tampilkanAngkaDiPanel(itemAngka)
    }

    private fun buatDaftarAngkaFase(): List<ItemAngka> {
        return when (faseSaatIni) {
            1 -> {
                targetMaksimal = 3
                listOf(1, 2, 3).map { ItemAngka(it, true) }
            }

            2 -> {
                targetMaksimal = 4
                listOf(1, 2, 3, 4).map { ItemAngka(it, true) }
            }

            else -> {
                targetMaksimal = 5
                listOf(1, 2, 3, 4, 5, 7).map { ItemAngka(it, it <= targetMaksimal) }
            }
        }
    }

    private fun tampilkanAngkaDiPanel(items: List<ItemAngka>) {
        val ukuran = when (faseSaatIni) {
            1 -> dpToPx(86)
            2 -> dpToPx(78)
            else -> dpToPx(68)
        }

        val rows = when (items.size) {
            3 -> listOf(items.take(2), items.drop(2))
            4 -> items.chunked(2)
            else -> items.chunked(3)
        }

        rows.forEachIndexed { rowIndex, rowItems ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    val topMargin = if (rowIndex == 0) 0 else dpToPx(28)
                    setMargins(0, topMargin, 0, 0)
                }
            }

            rowItems.forEachIndexed { columnIndex, item ->
                val tombolAngka = buatTombolAngka(item.angka, item.isTarget, ukuran)
                val params = LinearLayout.LayoutParams(ukuran, ukuran).apply {
                    val sideMargin = if (rowItems.size == 1) 0 else dpToPx(14)
                    setMargins(sideMargin, 0, sideMargin, 0)
                }

                row.addView(tombolAngka, params)

                val animationIndex = (rowIndex * 3) + columnIndex
                tombolAngka.scaleX = 0.4f
                tombolAngka.scaleY = 0.4f
                tombolAngka.alpha = 0f
                tombolAngka.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setStartDelay((animationIndex * 70L))
                    .setDuration(260L)
                    .setInterpolator(OvershootInterpolator())
                    .start()
            }

            containerAngka.addView(row)
        }
    }

    private fun buatTombolAngka(angka: Int, isTarget: Boolean, ukuran: Int): TextView {
        return TextView(this).apply {
            text = angka.toString()
            textSize = when {
                ukuran >= dpToPx(90) -> 52f
                ukuran >= dpToPx(80) -> 44f
                else -> 36f
            }
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            includeFontPadding = false
            elevation = dpToPx(12).toFloat()
            background = buatBackgroundAngka(angka, isTarget)
            setShadowLayer(3f, 0f, 2f, Color.parseColor("#66000000"))
            isClickable = true
            isFocusable = true

            setOnClickListener {
                prosesTapAngka(this, angka, isTarget)
            }
        }
    }

    private fun prosesTapAngka(view: TextView, angka: Int, isTarget: Boolean) {
        if (sesiSelesai || sedangTransisiRonde || !view.isEnabled) return

        if (!isTarget) {
            prosesJawabanSalah(view, "Itu angka pengecoh. Cari angka $angkaSelanjutnya dulu ya!")
            return
        }

        if (angka == angkaSelanjutnya) {
            prosesJawabanBenar(view)
        } else {
            prosesJawabanSalah(view, "Urut dari angka $angkaSelanjutnya dulu ya!")
        }
    }

    private fun prosesJawabanBenar(view: TextView) {
        view.isEnabled = false
        totalBenar++
        skor += 10
        angkaSelanjutnya++
        updateHud()

        view.animate()
            .scaleX(0.72f)
            .scaleY(0.72f)
            .alpha(0.28f)
            .translationY(dpToPx(10).toFloat())
            .setDuration(180L)
            .start()

        if (angkaSelanjutnya > targetMaksimal) {
            sedangTransisiRonde = true
            arenaGame.postDelayed({
                prosesAdaptifSetelahRondeSelesai()
            }, 450L)
        } else {
            updatePapanTarget()
        }
    }

    private fun prosesJawabanSalah(view: View, pesan: String) {
        totalSalah++
        rondeAdaSalah = true

        Toast.makeText(this, pesan, Toast.LENGTH_SHORT).show()

        view.animate()
            .translationX(dpToPx(8).toFloat())
            .setDuration(45L)
            .withEndAction {
                view.animate()
                    .translationX(dpToPx(-8).toFloat())
                    .setDuration(45L)
                    .withEndAction {
                        view.animate()
                            .translationX(0f)
                            .setDuration(45L)
                            .start()
                    }
                    .start()
            }
            .start()
    }

    private fun prosesAdaptifSetelahRondeSelesai() {
        if (sesiSelesai) return

        val rondeSempurna = !rondeAdaSalah
        val faseBaru = adaptiveManager.prosesJawaban(rondeSempurna)
        acakRondeBerikutnya = true

        if (faseBaru != faseSaatIni) {
            faseSaatIni = faseBaru
            Toast.makeText(this, "Fase berubah ke Fase $faseSaatIni", Toast.LENGTH_SHORT).show()
        }

        mulaiRonde()
    }

    private fun updateHud() {
        tvSkor.text = skor.toString()
        tvFase.text = faseSaatIni.toString()
    }

    private fun updatePapanTarget() {
        tvTargetAngka.text = "Ketuk angka $angkaSelanjutnya"
    }

    private fun buatBackgroundAngka(angka: Int, isTarget: Boolean): GradientDrawable {
        val warna = if (!isTarget) {
            intArrayOf(Color.parseColor("#FF7E96"), Color.parseColor("#EF445E"))
        } else {
            when (angka % 5) {
                1 -> intArrayOf(Color.parseColor("#52B9FF"), Color.parseColor("#2361D5"))
                2 -> intArrayOf(Color.parseColor("#A8E247"), Color.parseColor("#4C9D28"))
                3 -> intArrayOf(Color.parseColor("#FFD928"), Color.parseColor("#FF963E"))
                4 -> intArrayOf(Color.parseColor("#B074FF"), Color.parseColor("#6836D9"))
                else -> intArrayOf(Color.parseColor("#FF9A55"), Color.parseColor("#F05252"))
            }
        }

        return GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, warna).apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dpToPx(20).toFloat()
            setStroke(dpToPx(4), Color.WHITE)
        }
    }

    private fun simpanRiwayatAkhir() {
        if (sesiSelesai) return
        sesiSelesai = true

        timerPermainan?.cancel()
        containerAngka.removeAllViews()

        val totalJawaban = totalBenar + totalSalah

        val akurasi = if (totalJawaban > 0) {
            ((totalBenar * 100f) / totalJawaban).toInt()
        } else {
            0
        }

        val durasiMillis = System.currentTimeMillis() - waktuMulaiSesi
        val durasiDetik = maxOf(1, (durasiMillis / 1000L).toInt())
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
                intentToEvaluasi.putExtra("USIA_ANAK", usiaAnak)
                intentToEvaluasi.putExtra("NAMA_GAME", namaGame)
                intentToEvaluasi.putExtra("GAME_KEY", "urut_angka")
                intentToEvaluasi.putExtra("SKOR", skor)
                intentToEvaluasi.putExtra("AKURASI", akurasi)
                intentToEvaluasi.putExtra("DURASI_DETIK", durasiDetik)
                intentToEvaluasi.putExtra("FASE_AKHIR", faseSaatIni)
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
