package com.example.focusplay.games

import android.app.Dialog
import android.content.ClipData
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.DragEvent
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import com.example.focusplay.dashboard.DashboardAnakActivity
import com.example.focusplay.history.EvaluasiActivity
import com.example.focusplay.utils.AdaptiveGameManager
import com.example.focusplay.utils.GameResultHelper
import kotlin.random.Random

class GameAntarSiDombaActivity : AppCompatActivity() {

    private lateinit var btnMenuGame: TextView
    private lateinit var tvFase: TextView
    private lateinit var tvTimer: TextView
    private lateinit var tvSkor: TextView
    private lateinit var arenaGame: FrameLayout
    private lateinit var adaptiveManager: AdaptiveGameManager

    private var idAnak = ""
    private var namaAnak = "Anak"
    private var usiaAnak = 0

    private var faseSekarang = 1
    private var modeAdaptif = true
    private var skor = 0
    private var totalBenar = 0
    private var totalSalah = 0
    private var targetWaktuMenit = 1

    private var waktuMulaiSesi = 0L
    private var sesiSelesai = false
    private var countDownTimer: CountDownTimer? = null
    private var sisaWaktuMillis = 0L
    private var gameSedangPause = false

    private val namaGame = "Antar Si Domba"

    data class DombaRumah(
        val nama: String,
        val gambarDomba: Int,
        val gambarRumah: Int
    )

    private val daftarDombaRumah = listOf(
        DombaRumah("putih", R.drawable.domba_putih, R.drawable.rumah_putih),
        DombaRumah("pink", R.drawable.domba_pink, R.drawable.rumah_pink),
        DombaRumah("kuning", R.drawable.domba_kuning, R.drawable.rumah_kuning),
        DombaRumah("biru", R.drawable.domba_biru, R.drawable.rumah_biru)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_antar_si_domba)

        ambilDataAnakDariIntent()
        hubungkanView()
        bacaPengaturanGame()
        aturTombol()

        waktuMulaiSesi = System.currentTimeMillis()

        mulaiTimer()
        mulaiRonde()
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
        btnMenuGame = findViewById(R.id.btnMenuGame)
        tvFase = findViewById(R.id.tvFase)
        tvTimer = findViewById(R.id.tvTimer)
        tvSkor = findViewById(R.id.tvSkor)
        arenaGame = findViewById(R.id.arenaGame)
    }

    private fun bacaPengaturanGame() {
        val prefs = getSharedPreferences("pengaturan_permainan", MODE_PRIVATE)

        modeAdaptif = prefs.getBoolean("mode_adaptif", true)
        targetWaktuMenit = prefs.getString("target_waktu", "1")?.toIntOrNull() ?: 1

        faseSekarang = 1

        adaptiveManager = AdaptiveGameManager(
            faseSekarang = faseSekarang,
            modeAdaptifAktif = modeAdaptif
        )
    }

    private fun aturTombol() {
        pasangAnimasiTekan(btnMenuGame)

        btnMenuGame.setOnClickListener {
            tampilkanMenuGame()
        }
    }

    private fun tampilkanMenuGame() {
        pauseGame()

        val dialog = Dialog(this)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        val root = FrameLayout(this).apply {
            background = GameMenuStyle.createPanelBackground(this@GameAntarSiDombaActivity)
        }

        val menuContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(32), dp(22), dp(32), dp(22))
        }

        val btnResume = GameMenuStyle.createMenuButton(this, "Lanjutkan", "#55B94D", "#137530")
        val btnAbout = GameMenuStyle.createMenuButton(this, "Petunjuk", "#FF9F22", "#C96A12")
        val btnQuit = GameMenuStyle.createMenuButton(this, "Keluar", "#F55761", "#B92131")

        menuContainer.addView(btnResume)
        menuContainer.addView(btnAbout)
        menuContainer.addView(btnQuit)

        val menuParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
            leftMargin = dp(42)
            rightMargin = dp(42)
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
            countDownTimer?.cancel()
            finish()
        }

        dialog.setContentView(root)

        dialog.setOnShowListener {
            val width = (resources.displayMetrics.widthPixels * 0.80).toInt()
            val height = dp(340)

            dialog.window?.setLayout(width, height)
        }

        dialog.show()
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

    private fun tampilkanAboutGame() {
        tampilkanDialogInfoGame(
            isi = "Antar Si Domba adalah permainan mencocokkan domba dengan rumah sesuai warna.\n\n" +
                    "Game ini membantu anak melatih fokus, ketelitian, koordinasi tangan dan mata, serta kemampuan mengenali warna."
        )
    }

    private fun tampilkanDialogInfoGame(isi: String) {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(20), dp(22), dp(18))
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
            layoutParams = LinearLayout.LayoutParams(dp(42), dp(42))
        }

        val titleBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, 0, 0)
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
            setPadding(0, dp(4), 0, 0)
        }

        titleBox.addView(title)
        titleBox.addView(subtitle)
        header.addView(icon)
        header.addView(titleBox)

        val body = TextView(this).apply {
            text = isi
            textSize = 14f
            setTextColor(Color.parseColor("#4B5563"))
            setLineSpacing(dp(3).toFloat(), 1f)
            background = roundedDrawable("#F6FAFF", 20, "#E1ECFA")
            setPadding(dp(15), dp(14), dp(15), dp(14))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dp(18), 0, dp(16))
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
            setPadding(dp(22), dp(11), dp(22), dp(11))
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
            val maxWidth = dp(560)
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
            cornerRadius = dp(radiusDp).toFloat()
            setColor(Color.parseColor(fillColor))
            setStroke(dp(1), Color.parseColor(strokeColor))
        }
    }

    private fun circleDrawable(fillColor: String): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor(fillColor))
        }
    }

    private fun mulaiTimer() {
        sisaWaktuMillis = targetWaktuMenit * 60 * 1000L
        jalankanTimer()
    }

    private fun jalankanTimer() {
        countDownTimer?.cancel()

        countDownTimer = object : CountDownTimer(sisaWaktuMillis, 1000L) {
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
                selesaikanSesiDanSimpan()
            }
        }.start()
    }

    private fun pauseGame() {
        gameSedangPause = true
        countDownTimer?.cancel()
    }

    private fun resumeGame() {
        if (sesiSelesai) return

        gameSedangPause = false
        jalankanTimer()
    }

    private fun mulaiRonde() {
        if (sesiSelesai) return

        arenaGame.post {
            if (sesiSelesai) return@post
            arenaGame.removeAllViews()

            tvFase.text = "Fase $faseSekarang"
            tvSkor.text = "$skor"

            val itemAktif = when (faseSekarang) {
                1 -> daftarDombaRumah.take(2)
                2 -> daftarDombaRumah.take(3)
                else -> daftarDombaRumah.take(4)
            }

            tampilkanRumah(itemAktif)
            tampilkanDomba(itemAktif)
        }
    }

    private fun tampilkanRumah(itemAktif: List<DombaRumah>) {
        val jumlah = itemAktif.size
        val ukuranRumah = when (jumlah) {
            2 -> 95
            3 -> 85
            else -> 75
        }

        val jarak = 8
        val totalLebar = (jumlah * ukuranRumah) + ((jumlah - 1) * jarak)
        val startX = (arenaGame.width - dp(totalLebar)) / 2

        itemAktif.forEachIndexed { index, data ->
            val rumah = ImageView(this).apply {
                setImageResource(data.gambarRumah)
                scaleType = ImageView.ScaleType.FIT_CENTER
                tag = data.nama

                setOnDragListener { targetView, event ->
                    when (event.action) {
                        DragEvent.ACTION_DROP -> {
                            val warnaDomba = event.clipDescription.label.toString()
                            val warnaRumah = targetView.tag.toString()
                            cekJawaban(warnaDomba, warnaRumah)
                            true
                        }

                        else -> true
                    }
                }
            }

            val params = FrameLayout.LayoutParams(dp(ukuranRumah), dp(ukuranRumah))
            params.leftMargin = startX + dp(index * (ukuranRumah + jarak))
            params.topMargin = (arenaGame.height * 0.41f).toInt() + dp(24)

            arenaGame.addView(rumah, params)
        }
    }

    private fun tampilkanDomba(itemAktif: List<DombaRumah>) {
        val data = itemAktif.random()

        arenaGame.setOnDragListener { _, event ->
            val dombaDiseret = event.localState as? View

            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> true
                DragEvent.ACTION_DROP -> {
                    dombaDiseret?.visibility = View.VISIBLE
                    true
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    if (!event.result) {
                        dombaDiseret?.visibility = View.VISIBLE
                    }
                    true
                }
                else -> true
            }
        }

        val domba = ImageView(this).apply {
            setImageResource(data.gambarDomba)
            scaleType = ImageView.ScaleType.FIT_CENTER
            tag = data.nama

            setOnTouchListener { view, event ->
                if (gameSedangPause) return@setOnTouchListener true

                if (event.action == MotionEvent.ACTION_DOWN) {
                    val clipData = ClipData.newPlainText(data.nama, data.nama)
                    val shadow = View.DragShadowBuilder(view)

                    val dragDimulai = view.startDragAndDrop(
                        clipData,
                        shadow,
                        view,
                        0
                    )

                    if (dragDimulai) {
                        view.visibility = View.INVISIBLE
                    }

                    true
                } else {
                    false
                }
            }
        }

        val ukuranDomba = dp(72)
        val params = FrameLayout.LayoutParams(ukuranDomba, ukuranDomba)

        val minX = dp(24)
        val maxX = maxOf(minX + 1, arenaGame.width - ukuranDomba - dp(24))

        val minY = dp(470)
        val maxY = maxOf(minY + 1, arenaGame.height - ukuranDomba - dp(80))

        params.leftMargin = Random.nextInt(minX, maxX)
        params.topMargin = Random.nextInt(minY, maxY)

        arenaGame.addView(domba, params)
    }

    private fun cekJawaban(warnaDomba: String, warnaRumah: String) {
        if (sesiSelesai || gameSedangPause) return

        val benar = warnaDomba == warnaRumah

        if (benar) {
            skor += 10
            totalBenar++
            tampilkanToastAtas("Mendapatkan 10 poin")
        } else {
            totalSalah++
            tampilkanToastAtas("Belum mendapatkan poin")
        }

        tvSkor.text = "Skor: $skor"

        val faseBaru = adaptiveManager.prosesJawaban(benar)

        if (faseBaru != faseSekarang) {
            faseSekarang = faseBaru
        }

        mulaiRonde()
    }

    private fun tampilkanToastAtas(pesan: String) {
        android.widget.Toast.makeText(
            this,
            pesan,
            android.widget.Toast.LENGTH_SHORT
        ).apply {
            setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, dp(72))
            show()
        }
    }

    private fun selesaikanSesiDanSimpan() {
        if (sesiSelesai) return
        sesiSelesai = true

        countDownTimer?.cancel()
        
        arenaGame.post {
            arenaGame.removeAllViews()

            val totalJawaban = totalBenar + totalSalah

            val akurasi = if (totalJawaban > 0) {
                ((totalBenar * 100f) / totalJawaban).toInt()
            } else {
                0
            }

            val durasiMillis = (System.currentTimeMillis() - waktuMulaiSesi)
                .coerceAtLeast(1000L)
            val durasiDetik = (durasiMillis / 1000L).toInt()
            val durasiMenit = maxOf(1, (durasiMillis / 60000L).toInt())

            tampilkanHasilSementara(akurasi, durasiDetik, durasiMenit)
        }
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
        contentView.findViewById<TextView>(R.id.tvHasilFase).text = faseSekarang.toString()

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
            val maxWidth = dp(650)
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
            durasiDetik = durasiDetik,
            durasiMenit = durasiMenit,
            faseAkhir = faseSekarang
        ) { hasilAI ->
            val intent = Intent(this, EvaluasiActivity::class.java)
            intent.putExtra("ID_ANAK", idAnak)
            intent.putExtra("NAMA_ANAK", namaAnak)
            intent.putExtra("USIA_ANAK", usiaAnak)
            intent.putExtra("NAMA_GAME", namaGame)
            intent.putExtra("GAME_KEY", "antar_rumah")
            intent.putExtra("SKOR", skor)
            intent.putExtra("AKURASI", akurasi)
            intent.putExtra("DURASI_DETIK", durasiDetik)
            intent.putExtra("FASE_AKHIR", faseSekarang)
            intent.putExtra("EVALUASI_LANGSUNG", hasilAI)
            startActivity(intent)
            finish()
        }
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

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
