package com.example.focusplay.games

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import com.example.focusplay.dashboard.DashboardAnakActivity
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
    private var gameSedangPause = false
    private var sedangTransisiRonde = false
    private var rondeAdaSalah = false
    private var acakRondeBerikutnya = false

    private var timerPermainan: CountDownTimer? = null
    private var sisaWaktuMillis = 0L

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
        pasangAnimasiTekan(btnKembali)

        btnKembali.setOnClickListener {
            tampilkanMenuGame()
        }
    }

    private fun tampilkanMenuGame() {
        pauseGame()

        val dialog = Dialog(this)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        val root = FrameLayout(this).apply {
            background = getDrawable(R.drawable.latar_menu)
        }

        val menuContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dpToPx(28), dpToPx(20), dpToPx(28), dpToPx(20))
        }

        val btnResume = buatTombolMenu(R.drawable.btn_lanjutkan)
        val btnAbout = buatTombolMenu(R.drawable.btn_tentang)
        val btnQuit = buatTombolMenu(R.drawable.btn_keluar)

        menuContainer.addView(btnResume)
        menuContainer.addView(btnAbout)
        menuContainer.addView(btnQuit)

        val menuParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
            leftMargin = dpToPx(34)
            rightMargin = dpToPx(34)
        }

        root.addView(menuContainer, menuParams)

        btnResume.setOnClickListener {
            dialog.dismiss()
            resumeGame()
        }

        btnAbout.setOnClickListener {
            tampilkanAboutGame()
        }

        btnQuit.setOnClickListener {
            dialog.dismiss()
            timerPermainan?.cancel()
            finish()
        }

        dialog.setContentView(root)

        dialog.setOnShowListener {
            val width = (resources.displayMetrics.widthPixels * 0.80).toInt()
            val height = dpToPx(340)
            dialog.window?.setLayout(width, height)
        }

        dialog.show()
    }

    private fun buatTombolMenu(backgroundRes: Int): ImageView {
        val tombol = ImageView(this).apply {
            setImageResource(backgroundRes)
            scaleType = ImageView.ScaleType.FIT_CENTER
            adjustViewBounds = true
            isClickable = true
            isFocusable = true
        }

        pasangAnimasiTekan(tombol)

        tombol.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dpToPx(62)
        ).apply {
            setMargins(0, dpToPx(8), 0, dpToPx(8))
        }

        return tombol
    }

    private fun mulaiTimerGlobal() {
        sisaWaktuMillis = targetWaktuMenit * 60 * 1000L
        jalankanTimerGlobal()
    }

    private fun jalankanTimerGlobal() {
        timerPermainan?.cancel()

        timerPermainan = object : CountDownTimer(sisaWaktuMillis, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                sisaWaktuMillis = millisUntilFinished

                val detik = millisUntilFinished / 1000
                val menit = detik / 60
                val sisaDetik = detik % 60
                tvTimer.text = "${menit}:${sisaDetik.toString().padStart(2, '0')}"
            }

            override fun onFinish() {
                sisaWaktuMillis = 0L
                tvTimer.text = "0:00"
                simpanRiwayatAkhir()
            }
        }.start()
    }

    private fun pauseGame() {
        if (gameSedangPause || sesiSelesai) return

        gameSedangPause = true
        timerPermainan?.cancel()
    }

    private fun resumeGame() {
        if (!gameSedangPause || sesiSelesai) return

        gameSedangPause = false
        jalankanTimerGlobal()
    }

    private fun tampilkanAboutGame() {
        tampilkanDialogInfoGame(
            isi = "Urut Angka adalah permainan mengetuk angka sesuai urutan yang diminta.\n\n" +
                    "Game ini membantu anak melatih fokus, ketelitian, ingatan urutan, dan kontrol impuls."
        )
    }

    private fun tampilkanDialogInfoGame(isi: String) {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(22), dpToPx(20), dpToPx(22), dpToPx(18))
            background = roundedDrawable("#FFFDF8", 28, "#D8E5F5")
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val icon = TextView(this).apply {
            text = "?"
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            includeFontPadding = false
            setTextColor(Color.WHITE)
            background = circleDrawable("#5E7FE0")
            layoutParams = LinearLayout.LayoutParams(dpToPx(42), dpToPx(42))
        }

        val titleBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(12), 0, 0, 0)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val title = TextView(this).apply {
            text = "Tentang Game"
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#243447"))
            includeFontPadding = false
        }

        val subtitle = TextView(this).apply {
            text = "Petunjuk singkat untuk pendamping"
            textSize = 12f
            setTextColor(Color.parseColor("#7B8895"))
            setPadding(0, dpToPx(4), 0, 0)
        }

        titleBox.addView(title)
        titleBox.addView(subtitle)
        header.addView(icon)
        header.addView(titleBox)

        val body = TextView(this).apply {
            text = isi
            textSize = 14f
            setTextColor(Color.parseColor("#4B5563"))
            setLineSpacing(dpToPx(3).toFloat(), 1f)
            background = roundedDrawable("#F6FAFF", 20, "#E1ECFA")
            setPadding(dpToPx(15), dpToPx(14), dpToPx(15), dpToPx(14))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dpToPx(18), 0, dpToPx(16))
            }
        }

        val dialog = Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(root)
            setCancelable(true)
        }

        val button = TextView(this).apply {
            text = "Mengerti"
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            background = roundedDrawable("#5E7FE0", 18, "#5E7FE0")
            setPadding(dpToPx(22), dpToPx(11), dpToPx(22), dpToPx(11))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            pasangAnimasiTekan(this)
            setOnClickListener { dialog.dismiss() }
        }

        root.addView(header)
        root.addView(body)
        root.addView(button)

        dialog.setOnShowListener {
            val maxWidth = dpToPx(560)
            val screenWidth = resources.displayMetrics.widthPixels
            dialog.window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setDimAmount(0.45f)
                addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                setLayout(
                    minOf((screenWidth * 0.88f).toInt(), maxWidth),
                    WindowManager.LayoutParams.WRAP_CONTENT
                )
            }
        }

        dialog.show()
    }

    private fun roundedDrawable(fillColor: String, radiusDp: Int, strokeColor: String): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dpToPx(radiusDp).toFloat()
            setColor(Color.parseColor(fillColor))
            setStroke(dpToPx(1), Color.parseColor(strokeColor))
        }
    }

    private fun circleDrawable(fillColor: String): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor(fillColor))
        }
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
        tvFase.text = "Fase $faseSaatIni"
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

        tampilkanHasilSementara(akurasi, durasiDetik, durasiMenit)
    }

    private fun tampilkanHasilSementara(
        akurasi: Int,
        durasiDetik: Int,
        durasiMenit: Int
    ) {
        val contentView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_hasil_sementara, null, false)

        contentView.findViewById<TextView>(R.id.tvHasilSkor).text = skor.toString()
        contentView.findViewById<TextView>(R.id.tvHasilAkurasi).text = "$akurasi%"
        contentView.findViewById<TextView>(R.id.tvHasilDurasi).text = "$durasiDetik detik"
        contentView.findViewById<TextView>(R.id.tvHasilFase).text = faseSaatIni.toString()

        val dialog = Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(contentView)
            setCancelable(false)
        }

        contentView.findViewById<TextView>(R.id.btnSimpanLihatHasil).apply {
            pasangAnimasiTekan(this)
            setOnClickListener {
                isEnabled = false
                dialog.dismiss()
                simpanDanBukaEvaluasi(akurasi, durasiDetik, durasiMenit)
            }
        }

        contentView.findViewById<TextView>(R.id.btnKembaliDashboard).apply {
            pasangAnimasiTekan(this)
            setOnClickListener {
                dialog.dismiss()
                kembaliKeDashboard()
            }
        }

        dialog.setOnShowListener {
            val maxWidth = dpToPx(650)
            val screenWidth = resources.displayMetrics.widthPixels
            dialog.window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setDimAmount(0.55f)
                addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                setLayout(
                    minOf((screenWidth * 0.92f).toInt(), maxWidth),
                    WindowManager.LayoutParams.WRAP_CONTENT
                )
            }
        }

        dialog.show()
    }

    private fun simpanDanBukaEvaluasi(
        akurasi: Int,
        durasiDetik: Int,
        durasiMenit: Int
    ) {
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

    private fun kembaliKeDashboard() {
        val intent = Intent(this, DashboardAnakActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("ID_ANAK", idAnak)
            putExtra("NAMA_ANAK", namaAnak)
            putExtra("USIA_ANAK", usiaAnak)
        }
        startActivity(intent)
        finish()
    }

    private fun pasangAnimasiTekan(view: View) {
        view.isClickable = true
        view.isFocusable = true

        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate()
                        .scaleX(0.94f)
                        .scaleY(0.94f)
                        .setDuration(45)
                        .start()
                }

                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(60)
                        .start()
                }
            }

            false
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
        timerPermainan?.cancel()
    }
}
