package com.example.focusplay.games

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import com.example.focusplay.dashboard.DashboardAnakActivity
import com.example.focusplay.history.EvaluasiActivity
import com.example.focusplay.utils.AdaptiveGameManager
import com.example.focusplay.utils.GameResultHelper
import kotlin.math.min

/**
 * Halaman game mencocokkan kartu dengan tampilan activity_game_pasang_kartu.xml.
 *
 * Data anak diterima melalui Intent, sedangkan waktu dan mode adaptif diambil dari pengaturan.
 * Setelah selesai, hasil dikirim ke GameResultHelper lalu diteruskan ke EvaluasiActivity.
 */
class GamePasangKartuActivity : AppCompatActivity() {

    // ==================== BAGIAN ELEMEN TAMPILAN ====================
    // Tombol untuk membuka menu permainan.
    private lateinit var btnMenuGame: ImageView
    // Grid tempat kartu dibuat dan ditambahkan.
    private lateinit var gridKartu: GridLayout
    // Menampilkan skor permainan.
    private lateinit var tvSkor: TextView
    // Menampilkan fase aktif.
    private lateinit var tvFase: TextView
    // Menampilkan sisa waktu permainan.
    private lateinit var tvTimer: TextView
    // Menampilkan petunjuk sesuai kondisi sesi.
    private lateinit var tvPetunjuk: TextView
    // Mengatur perubahan fase berdasarkan hasil sesi.
    private lateinit var adaptiveManager: AdaptiveGameManager

    // ==================== BAGIAN DATA PERMAINAN ====================
    // Nilai skor yang bertambah ketika pasangan ditemukan.
    private var skor = 0
    // Fase permainan yang sedang aktif.
    private var faseSaatIni = 1
    // Identitas anak yang diterima dari halaman sebelumnya.
    private var idAnak = ""
    private var namaAnak = "Anak"
    private var usiaAnak = 0

    // Nilai pengaturan mode adaptif.
    private var modeAdaptif = true
    // Lama permainan berdasarkan pengaturan pengguna.
    private var targetWaktuMenit = 1

    private var totalBenar = 0
    private var totalSalah = 0
    private var waktuMulaiPermainan = 0L
    // Menandai bahwa permainan sudah selesai agar hasil tidak diproses dua kali.
    private var permainanSelesai = false

    // ==================== BAGIAN STATUS KARTU DAN TIMER ====================
    private var jumlahPasanganSelesai = 0
    private var totalPasanganSesiIni = 0
    private var sesiAdaKesalahan = false
    private var sedangPreview = false
    private var sedangMemeriksa = false

    // Referensi dua kartu yang sedang dibandingkan.
    private var kartuPertama: FrameLayout? = null
    private var kartuKedua: FrameLayout? = null

    // Timer utama untuk batas durasi permainan.
    private var timerPermainan: CountDownTimer? = null
    private var sisaWaktuMillis = 0L
    private var gameSedangPause = false

    // Handler menjalankan proses tertunda seperti menutup kartu.
    private val handler = Handler(Looper.getMainLooper())
    private var aksiTertunda: Runnable? = null
    private var batasWaktuAksiTertunda = 0L
    private var sisaDelayAksiTertunda = 0L

    // Nama game yang dikirim ke riwayat dan evaluasi.
    private val namaGame = "Pasang Kartu"

    // ==================== BAGIAN DATA KARTU ====================
    // Menyimpan ID pasangan, gambar, dan status satu kartu.
    private data class DataKartu(
        val kode: String,
        val gambar: Int,
        var terbuka: Boolean = true,
        var selesai: Boolean = false
    )

    // Daftar gambar yang dapat dipakai sebagai pasangan kartu.
    private val daftarGambar = listOf(
        Pair("heart", R.drawable.char_heart),
        Pair("star", R.drawable.char_star),
        Pair("moon", R.drawable.char_moon_purple),
        Pair("mushroom", R.drawable.char_mushroom),
        Pair("diamond", R.drawable.char_diamond_orange),
        Pair("cucumber", R.drawable.char_cucumber)
    )

    // ==================== BAGIAN INISIALISASI GAME ====================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Memasang activity_game_pasang_kartu.xml.
        setContentView(R.layout.activity_game_pasang_kartu)

        // Menyiapkan seluruh data dan aksi sebelum sesi dimulai.
        ambilDataAnakDariIntent()
        hubungkanView()
        bacaPengaturan()
        aturTombol()

        // Menyimpan waktu mulai untuk menghitung durasi aktual.
        waktuMulaiPermainan = System.currentTimeMillis()
        mulaiTimerGlobal()

        // Menunggu grid selesai diukur agar ukuran kartu dapat dihitung.
        gridKartu.post {
            mulaiSesiBaru()
        }
    }

    // ==================== BAGIAN DATA INTENT DAN PENGATURAN ====================
    private fun ambilDataAnakDariIntent() {
        // Mengambil data extra dari halaman anak yang membuka game.
        // Menerima ID dengan dukungan key huruf besar dan kecil.
        idAnak = intent.getStringExtra("ID_ANAK")
            ?: intent.getStringExtra("id_anak")
            ?: ""

        // Menerima nama anak dengan nilai cadangan "Anak".
        namaAnak = intent.getStringExtra("NAMA_ANAK")
            ?: intent.getStringExtra("nama_anak")
            ?: "Anak"

        // Menerima usia anak untuk diteruskan ke halaman evaluasi.
        usiaAnak = intent.getIntExtra(
            "USIA_ANAK",
            intent.getIntExtra("usia_anak", 0)
        )
    }

    private fun hubungkanView() {
        // Menghubungkan ID dengan status dan papan kartu pada layout XML.
        // Menghubungkan seluruh elemen status game dari XML.
        btnMenuGame = findViewById(R.id.btnMenuGame)
        gridKartu = findViewById(R.id.gridKartu)
        tvSkor = findViewById(R.id.tvSkor)
        tvFase = findViewById(R.id.tvFase)
        tvTimer = findViewById(R.id.tvTimer)
        tvPetunjuk = findViewById(R.id.tvPetunjuk)
    }

    private fun bacaPengaturan() {
        // Nama file dan key sama dengan yang disimpan PengaturanPermainanActivity.
        // Membuka SharedPreferences yang ditulis halaman pengaturan.
        val prefs = getSharedPreferences("pengaturan_permainan", MODE_PRIVATE)

        // Mengambil mode adaptif dan target waktu beserta nilai default.
        modeAdaptif = prefs.getBoolean("mode_adaptif", true)
        targetWaktuMenit = prefs.getString("target_waktu", "1")?.toIntOrNull() ?: 1

        // Setiap permainan selalu dimulai dari fase 1.
        faseSaatIni = 1
        // Membuat manager adaptif dengan pengaturan yang sudah dibaca.
        adaptiveManager = AdaptiveGameManager(
            faseSekarang = faseSaatIni,
            modeAdaptifAktif = modeAdaptif
        )
    }

    // ==================== BAGIAN MENU PERMAINAN ====================
    private fun aturTombol() {
        // Memberi animasi tekan pada tombol menu.
        pasangAnimasiTekan(btnMenuGame)

        btnMenuGame.setOnClickListener {
            tampilkanMenuGame()
        }
    }

    private fun tampilkanMenuGame() {
        // Menjeda game sebelum dialog menu ditampilkan.
        pauseGame()

        // Membuat dialog menu secara dinamis.
        val dialog = Dialog(this)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        val root = FrameLayout(this).apply {
            background = GameMenuStyle.createPanelBackground(this@GamePasangKartuActivity)
        }

        val menuContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dpToPx(32), dpToPx(22), dpToPx(32), dpToPx(22))
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
            leftMargin = dpToPx(42)
            rightMargin = dpToPx(42)
        }

        root.addView(menuContainer, menuParams)

        // Tombol Lanjutkan menutup dialog dan melanjutkan timer.
        btnResume.setOnClickListener {
            dialog.dismiss()
            resumeGame()
        }

        btnAbout.setOnClickListener {
            tampilkanAboutGame()
        }

        // Tombol Keluar menghentikan proses game dan menutup Activity.
        btnQuit.setOnClickListener {
            dialog.dismiss()
            hentikanProsesBerjalan()
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
            isi = "Pasang Kartu adalah permainan mengingat posisi gambar dan mencocokkan kartu yang sama.\n\n" +
                    "Game ini membantu anak melatih daya ingat, fokus, dan ketelitian."
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

    // ==================== BAGIAN TIMER DAN PAUSE ====================
    private fun mulaiTimerGlobal() {
        // Lama timer diambil dari target waktu yang tersimpan di SharedPreferences.
        // Mengubah target menit menjadi milidetik.
        sisaWaktuMillis = targetWaktuMenit * 60 * 1000L
        tvTimer.visibility = View.VISIBLE
        jalankanTimerGlobal()
    }

    private fun jalankanTimerGlobal() {
        // Membatalkan timer lama sebelum membuat timer baru.
        timerPermainan?.cancel()

        timerPermainan = object : CountDownTimer(sisaWaktuMillis, 1000L) {
            // Memperbarui sisa waktu setiap satu detik.
            override fun onTick(millisUntilFinished: Long) {
                sisaWaktuMillis = millisUntilFinished

                val totalDetik = millisUntilFinished / 1000L
                val menit = totalDetik / 60L
                val detik = totalDetik % 60L
                tvTimer.text = "$menit:${detik.toString().padStart(2, '0')}"
            }

            // Menyimpan hasil akhir ketika waktu habis.
            override fun onFinish() {
                sisaWaktuMillis = 0L
                tvTimer.text = "0:00"
                simpanRiwayatAkhir()
            }
        }.start()
    }

    private fun pauseGame() {
        // Pause diabaikan jika game sudah pause atau selesai.
        if (gameSedangPause || permainanSelesai) return

        gameSedangPause = true
        timerPermainan?.cancel()

        // Menyimpan sisa delay proses kartu yang sedang menunggu.
        aksiTertunda?.let { runnable ->
            handler.removeCallbacks(runnable)
            sisaDelayAksiTertunda =
                (batasWaktuAksiTertunda - System.currentTimeMillis()).coerceAtLeast(0L)
        }
    }

    private fun resumeGame() {
        // Resume hanya berlaku jika game memang sedang pause.
        if (!gameSedangPause || permainanSelesai) return

        gameSedangPause = false
        jalankanTimerGlobal()

        aksiTertunda?.let { runnable ->
            batasWaktuAksiTertunda = System.currentTimeMillis() + sisaDelayAksiTertunda
            handler.postDelayed(runnable, sisaDelayAksiTertunda)
        }
    }

    private fun jadwalkanAksi(delayMillis: Long, aksi: () -> Unit) {
        // Membatalkan aksi tertunda sebelumnya agar tidak berjalan ganda.
        aksiTertunda?.let { handler.removeCallbacks(it) }

        val runnable = Runnable {
            aksiTertunda = null
            sisaDelayAksiTertunda = 0L

            if (!permainanSelesai) {
                aksi()
            }
        }

        aksiTertunda = runnable
        sisaDelayAksiTertunda = delayMillis
        batasWaktuAksiTertunda = System.currentTimeMillis() + delayMillis

        if (!gameSedangPause) {
            handler.postDelayed(runnable, delayMillis)
        }
    }

    // ==================== BAGIAN MEMBUAT SESI KARTU ====================
    private fun mulaiSesiBaru() {
        // Menghentikan pembuatan sesi jika permainan sudah selesai.
        if (permainanSelesai) return
        // Menghapus seluruh kartu sesi sebelumnya.
        gridKartu.removeAllViews()
        jumlahPasanganSelesai = 0
        sesiAdaKesalahan = false
        sedangPreview = true
        sedangMemeriksa = false
        kartuPertama = null
        kartuKedua = null

        // Fase menentukan ukuran grid, jumlah pasangan, dan waktu melihat kartu.
        val waktuPreview = aturTampilanFase()
        // Membuat daftar kartu acak sesuai jumlah pasangan fase.
        val daftarKartu = buatDaftarKartuAcak()
        val (lebarKartu, tinggiKartu) = hitungUkuranKartu()

        // Membuat dan menampilkan setiap kartu dalam keadaan terbuka.
        daftarKartu.forEach { data ->
            val kartu = buatViewKartu(data, lebarKartu, tinggiKartu)
            gridKartu.addView(kartu)
            tampilkanKartuTerbuka(kartu)
        }

        tvPetunjuk.text = "Ingat letak gambarnya, lalu cocokkan kartu yang sama!"

        // Setelah waktu preview selesai, semua kartu ditutup dan dapat diklik.
        jadwalkanAksi(waktuPreview) {
            sedangPreview = false
            for (i in 0 until gridKartu.childCount) {
                val kartu = gridKartu.getChildAt(i) as FrameLayout
                tampilkanKartuTertutup(kartu)
                kartu.isClickable = true
                kartu.isFocusable = true
            }

            tvPetunjuk.text = "Sekarang cari dan pasangkan gambar yang sama!"
        }
    }

    private fun aturTampilanFase(): Long {
        // Mengatur jumlah kolom, baris, pasangan, dan waktu preview per fase.
        return when (faseSaatIni) {
            1 -> {
                tvFase.text = "Fase 1"
                gridKartu.columnCount = 2
                gridKartu.rowCount = 2
                totalPasanganSesiIni = 2
                3500L
            }

            2 -> {
                tvFase.text = "Fase 2"
                gridKartu.columnCount = 4
                gridKartu.rowCount = 2
                totalPasanganSesiIni = 4
                3000L
            }

            else -> {
                tvFase.text = "Fase 3"
                gridKartu.columnCount = 4
                gridKartu.rowCount = 3
                totalPasanganSesiIni = 6
                2500L
            }
        }
    }

    private fun buatDaftarKartuAcak(): MutableList<DataKartu> {
        val hasil = mutableListOf<DataKartu>()

        // Mengambil gambar sesuai jumlah pasangan pada fase.
        daftarGambar.take(totalPasanganSesiIni).forEach { (kode, gambar) ->
            hasil.add(DataKartu(kode, gambar))
            hasil.add(DataKartu(kode, gambar))
        }

        // Mengacak posisi seluruh kartu sebelum ditampilkan.
        hasil.shuffle()
        return hasil
    }

    private fun buatViewKartu(
        data: DataKartu,
        lebarKartu: Int,
        tinggiKartu: Int
    ): FrameLayout {
        // Setiap kartu dibuat melalui kode lalu dimasukkan ke gridKartu.
        val kartu = FrameLayout(this).apply {
            layoutParams = GridLayout.LayoutParams().apply {
                width = lebarKartu
                height = tinggiKartu
                setMargins(dpToPx(5), dpToPx(5), dpToPx(5), dpToPx(5))
            }

        // Menyimpan DataKartu pada tag agar dapat diambil saat kartu diklik.
            tag = data
            elevation = dpToPx(5).toFloat()
            isClickable = false
            isFocusable = false
            contentDescription = "Kartu permainan"
        }

        val gambar = ImageView(this).apply {
            id = R.id.img_isi_kartu
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.FIT_CENTER
            setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
        }

        val tandaTanya = TextView(this).apply {
            id = R.id.tv_tanda_tanya_kartu
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            gravity = Gravity.CENTER
            text = "?"
            textSize = 34f
            setTextColor(Color.WHITE)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            visibility = View.GONE
        }

        kartu.addView(gambar)
        kartu.addView(tandaTanya)

        // Menghubungkan klik kartu ke proses pilihKartu().
        kartu.setOnClickListener {
            pilihKartu(kartu)
        }

        return kartu
    }

    // ==================== BAGIAN TAMPILAN KARTU ====================
    private fun tampilkanKartuTerbuka(kartu: FrameLayout) {
        val data = kartu.tag as DataKartu
        val gambar = kartu.findViewById<ImageView>(R.id.img_isi_kartu)
        val tandaTanya = kartu.findViewById<TextView>(R.id.tv_tanda_tanya_kartu)

        data.terbuka = true
        kartu.background = buatBackgroundKartu("#FFFDF4", "#F5A94F")
        gambar.setImageResource(data.gambar)
        gambar.visibility = View.VISIBLE
        tandaTanya.visibility = View.GONE
    }

    private fun tampilkanKartuTertutup(kartu: FrameLayout) {
        val data = kartu.tag as DataKartu
        val gambar = kartu.findViewById<ImageView>(R.id.img_isi_kartu)
        val tandaTanya = kartu.findViewById<TextView>(R.id.tv_tanda_tanya_kartu)

        data.terbuka = false
        kartu.background = buatBackgroundKartu("#63C7F5", "#258BCB")
        gambar.visibility = View.GONE
        tandaTanya.visibility = View.VISIBLE
    }

    private fun tampilkanKartuBenar(kartu: FrameLayout) {
        val data = kartu.tag as DataKartu
        val gambar = kartu.findViewById<ImageView>(R.id.img_isi_kartu)
        val tandaTanya = kartu.findViewById<TextView>(R.id.tv_tanda_tanya_kartu)

        data.terbuka = true
        data.selesai = true
        kartu.isClickable = false
        kartu.background = buatBackgroundKartu("#F0FFE1", "#88B943")
        gambar.setImageResource(data.gambar)
        gambar.visibility = View.VISIBLE
        tandaTanya.visibility = View.GONE
    }

    // ==================== BAGIAN CEK PASANGAN DAN FASE ====================
    private fun pilihKartu(kartu: FrameLayout) {
        // Mengabaikan klik ketika game tidak siap menerima pilihan.
        if (permainanSelesai || gameSedangPause || sedangPreview || sedangMemeriksa) return
        if (kartu == kartuPertama) return

        val data = kartu.tag as DataKartu
        if (data.terbuka || data.selesai) return

        // Membuka kartu yang baru dipilih.
        tampilkanKartuTerbuka(kartu)

        if (kartuPertama == null) {
            // Menyimpan pilihan pertama.
            kartuPertama = kartu
        } else {
            // Menyimpan pilihan kedua lalu membandingkan pasangan.
            kartuKedua = kartu
            cekPasangan()
        }
    }

    private fun cekPasangan() {
        sedangMemeriksa = true

        // Mengambil data dari kedua kartu.
        val dataPertama = kartuPertama?.tag as? DataKartu
        val dataKedua = kartuKedua?.tag as? DataKartu
        // Membandingkan kode untuk menentukan apakah gambar sama.
        val pasanganSama = dataPertama?.kode == dataKedua?.kode

        if (pasanganSama) {
            kartuPertama?.let { tampilkanKartuBenar(it) }
            kartuKedua?.let { tampilkanKartuBenar(it) }

            // Menambah jumlah pasangan, jawaban benar, dan skor.
            jumlahPasanganSelesai++
            totalBenar++
            skor += 15
            tvSkor.text = skor.toString()

            jadwalkanAksi(450L) {
                resetPilihan()

                if (jumlahPasanganSelesai == totalPasanganSesiIni) {
                    prosesSesiSelesai()
                }
            }
        } else {
            // Menandai sesi memiliki kesalahan dan menambah total salah.
            sesiAdaKesalahan = true
            totalSalah++

            jadwalkanAksi(750L) {
                kartuPertama?.let { tampilkanKartuTertutup(it) }
                kartuKedua?.let { tampilkanKartuTertutup(it) }
                resetPilihan()
            }
        }
    }

    private fun prosesSesiSelesai() {
        if (permainanSelesai) return

        // Satu sesi dinilai benar hanya jika seluruh pasangan diselesaikan tanpa salah pilih.
        // Dengan begitu, adaptif dihitung per sesi, bukan per kartu yang ditekan.
        val sesiBenar = !sesiAdaKesalahan
        // Mengirim hasil sesi ke AdaptiveGameManager untuk menentukan fase berikutnya.
        val faseBaru = adaptiveManager.prosesJawaban(sesiBenar)

        if (faseBaru != faseSaatIni) {
            faseSaatIni = faseBaru
            Toast.makeText(this, "Fase berubah ke Fase $faseSaatIni", Toast.LENGTH_SHORT).show()
        }

        tvPetunjuk.text = "Bagus! Bersiap untuk sesi berikutnya..."

        jadwalkanAksi(900L) {
            mulaiSesiBaru()
        }
    }

    private fun resetPilihan() {
        kartuPertama = null
        kartuKedua = null
        sedangMemeriksa = false
    }

    private fun hitungUkuranKartu(): Pair<Int, Int> {
        val jumlahKolom = gridKartu.columnCount.coerceAtLeast(2)
        val lebarLayar = resources.displayMetrics.widthPixels
        val ruangHorizontal = dpToPx(68)
        val marginAntarKartu = dpToPx(10)
        val lebarMaksimal = if (jumlahKolom == 2) dpToPx(118) else dpToPx(78)
        val lebarDariLayar = ((lebarLayar - ruangHorizontal) / jumlahKolom) - marginAntarKartu
        val lebar = min(lebarMaksimal, lebarDariLayar).coerceAtLeast(dpToPx(58))
        val tinggi = (lebar * 1.18f).toInt()
        return Pair(lebar, tinggi)
    }

    private fun buatBackgroundKartu(warnaIsi: String, warnaStroke: String): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dpToPx(18).toFloat()
            setColor(Color.parseColor(warnaIsi))
            setStroke(dpToPx(3), Color.parseColor(warnaStroke))
        }
    }

    // ==================== BAGIAN HASIL AKHIR PERMAINAN ====================
    private fun simpanRiwayatAkhir() {
        // Dijalankan saat timer habis sebelum menampilkan ringkasan hasil.
        if (permainanSelesai) return
        permainanSelesai = true

        hentikanProsesBerjalan()
        gridKartu.removeAllViews()

        // Menghitung akurasi berdasarkan total benar dan salah.
        val totalJawaban = totalBenar + totalSalah
        val akurasi = if (totalJawaban > 0) {
            ((totalBenar * 100f) / totalJawaban).toInt()
        } else {
            0
        }

        // Menghitung durasi aktual dari waktu mulai sampai sekarang.
        val durasiMillis = System.currentTimeMillis() - waktuMulaiPermainan
        val durasiDetik = maxOf(1, (durasiMillis / 1000L).toInt())
        val durasiMenit = maxOf(1, (durasiMillis / 60000L).toInt())

        // Menampilkan dialog ringkasan sebelum pengguna memilih tindakan.
        tampilkanHasilSementara(akurasi, durasiDetik, durasiMenit)
    }

    private fun tampilkanHasilSementara(
        akurasi: Int,
        durasiDetik: Int,
        durasiMenit: Int
    ) {
        val contentView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_hasil_sementara, null, false)

        // Mengisi seluruh angka hasil ke dialog_hasil_sementara.xml.
        contentView.findViewById<TextView>(R.id.tvHasilSkor).text = skor.toString()
        contentView.findViewById<TextView>(R.id.tvHasilAkurasi).text = "$akurasi%"
        contentView.findViewById<TextView>(R.id.tvHasilDurasi).text = "$durasiDetik detik"
        contentView.findViewById<TextView>(R.id.tvHasilFase).text = faseSaatIni.toString()

        val dialog = Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(contentView)
            setCancelable(false)
        }

        // Tombol utama menyimpan hasil dan membuka evaluasi.
        contentView.findViewById<TextView>(R.id.btnSimpanLihatHasil).apply {
            pasangAnimasiTekan(this)
            setOnClickListener {
                isEnabled = false
                dialog.dismiss()
                simpanDanBukaEvaluasi(akurasi, durasiDetik, durasiMenit)
            }
        }

        // Tombol kedua kembali tanpa membuka halaman evaluasi.
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

    // ==================== BAGIAN PINDAH HALAMAN ====================
    private fun simpanDanBukaEvaluasi(
        akurasi: Int,
        durasiDetik: Int,
        durasiMenit: Int
    ) {
        // Mengirim data sesi untuk dievaluasi AI dan disimpan ke "tb_riwayat".
        GameResultHelper.evaluasiDanSimpanRealtime(
            activity = this,
            idAnak = idAnak,
            namaAnak = namaAnak,
            namaGame = namaGame,
            skor = skor,
            akurasi = akurasi,
            durasiDetik = durasiDetik,
            durasiMenit = durasiMenit,
            faseAkhir = faseSaatIni,
            onSelesai = { hasilEvaluasi ->
                // Mengirim statistik dan hasil evaluasi ke EvaluasiActivity melalui Intent.
                val intentToEvaluasi = Intent(this, EvaluasiActivity::class.java)
                intentToEvaluasi.putExtra("ID_ANAK", idAnak)
                intentToEvaluasi.putExtra("NAMA_ANAK", namaAnak)
                intentToEvaluasi.putExtra("USIA_ANAK", usiaAnak)
                intentToEvaluasi.putExtra("NAMA_GAME", namaGame)
                intentToEvaluasi.putExtra("GAME_KEY", "pasang_kartu")
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
        // Kembali ke Dashboard Anak dengan membawa identitas anak yang sedang bermain.
        val intent = Intent(this, DashboardAnakActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("ID_ANAK", idAnak)
            putExtra("NAMA_ANAK", namaAnak)
            putExtra("USIA_ANAK", usiaAnak)
        }
        startActivity(intent)
        finish()
    }

    // ==================== BAGIAN PEMBERSIHAN PROSES ====================
    private fun hentikanProsesBerjalan() {
        timerPermainan?.cancel()
        aksiTertunda?.let { handler.removeCallbacks(it) }
        aksiTertunda = null
        handler.removeCallbacksAndMessages(null)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
        hentikanProsesBerjalan()
    }
}
