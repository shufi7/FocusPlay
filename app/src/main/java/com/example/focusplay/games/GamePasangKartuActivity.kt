package com.example.focusplay.games

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import com.example.focusplay.history.EvaluasiActivity
import com.example.focusplay.utils.AdaptiveGameManager
import com.example.focusplay.utils.GameResultHelper
import kotlin.math.min

class GamePasangKartuActivity : AppCompatActivity() {

    private lateinit var gridKartu: GridLayout
    private lateinit var tvSkor: TextView
    private lateinit var tvFase: TextView
    private lateinit var tvTimer: TextView
    private lateinit var tvPetunjuk: TextView
    private lateinit var adaptiveManager: AdaptiveGameManager

    private var skor = 0
    private var faseSaatIni = 1
    private var idAnak = ""
    private var namaAnak = "Anak"

    private var modeAdaptif = true
    private var targetWaktuMenit = 1

    private var totalBenar = 0
    private var totalSalah = 0
    private var waktuMulaiPermainan = 0L
    private var permainanSelesai = false

    private var jumlahPasanganSelesai = 0
    private var totalPasanganSesiIni = 0
    private var sesiAdaKesalahan = false
    private var sedangPreview = false
    private var sedangMemeriksa = false

    private var kartuPertama: FrameLayout? = null
    private var kartuKedua: FrameLayout? = null

    private var timerPermainan: CountDownTimer? = null
    private val handler = Handler(Looper.getMainLooper())

    private val namaGame = "Pasang Kartu"

    private data class DataKartu(
        val kode: String,
        val gambar: Int,
        var terbuka: Boolean = true,
        var selesai: Boolean = false
    )

    private val daftarGambar = listOf(
        Pair("heart", R.drawable.char_heart),
        Pair("star", R.drawable.char_star),
        Pair("moon", R.drawable.char_moon_purple),
        Pair("mushroom", R.drawable.char_mushroom),
        Pair("diamond", R.drawable.char_diamond_orange),
        Pair("cucumber", R.drawable.char_cucumber)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_pasang_kartu)

        ambilDataAnakDariIntent()
        hubungkanView()
        bacaPengaturan()
        aturTombol()

        waktuMulaiPermainan = System.currentTimeMillis()
        mulaiTimerGlobal()

        gridKartu.post {
            mulaiSesiBaru()
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
        gridKartu = findViewById(R.id.gridKartu)
        tvSkor = findViewById(R.id.tvSkor)
        tvFase = findViewById(R.id.tvFase)
        tvTimer = findViewById(R.id.tvTimer)
        tvPetunjuk = findViewById(R.id.tvPetunjuk)
    }

    private fun bacaPengaturan() {
        val prefs = getSharedPreferences("pengaturan_permainan", MODE_PRIVATE)

        modeAdaptif = prefs.getBoolean("mode_adaptif", true)
        targetWaktuMenit = prefs.getString("target_waktu", "1")?.toIntOrNull() ?: 1

        // Setiap permainan selalu dimulai dari fase 1.
        faseSaatIni = 1
        adaptiveManager = AdaptiveGameManager(
            faseSekarang = faseSaatIni,
            modeAdaptifAktif = modeAdaptif
        )
    }

    private fun aturTombol() {
        findViewById<ImageView>(R.id.btnKembali).setOnClickListener {
            hentikanProsesBerjalan()
            finish()
        }
    }

    private fun mulaiTimerGlobal() {
        val totalMillis = targetWaktuMenit * 60 * 1000L
        tvTimer.visibility = View.VISIBLE

        timerPermainan = object : CountDownTimer(totalMillis, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val totalDetik = millisUntilFinished / 1000L
                val menit = totalDetik / 60L
                val detik = totalDetik % 60L
                tvTimer.text = "$menit:${detik.toString().padStart(2, '0')}"
            }

            override fun onFinish() {
                tvTimer.text = "0:00"
                simpanRiwayatAkhir()
            }
        }.start()
    }

    private fun mulaiSesiBaru() {
        if (permainanSelesai) return
        gridKartu.removeAllViews()
        jumlahPasanganSelesai = 0
        sesiAdaKesalahan = false
        sedangPreview = true
        sedangMemeriksa = false
        kartuPertama = null
        kartuKedua = null

        val waktuPreview = aturTampilanFase()
        val daftarKartu = buatDaftarKartuAcak()
        val (lebarKartu, tinggiKartu) = hitungUkuranKartu()

        daftarKartu.forEach { data ->
            val kartu = buatViewKartu(data, lebarKartu, tinggiKartu)
            gridKartu.addView(kartu)
            tampilkanKartuTerbuka(kartu)
        }

        tvPetunjuk.text = "Ingat letak gambarnya, lalu cocokkan kartu yang sama!"

        handler.postDelayed({
            if (permainanSelesai) return@postDelayed

            sedangPreview = false
            for (i in 0 until gridKartu.childCount) {
                val kartu = gridKartu.getChildAt(i) as FrameLayout
                tampilkanKartuTertutup(kartu)
                kartu.isClickable = true
                kartu.isFocusable = true
            }

            tvPetunjuk.text = "Sekarang cari dan pasangkan gambar yang sama!"
        }, waktuPreview)
    }

    private fun aturTampilanFase(): Long {
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

        daftarGambar.take(totalPasanganSesiIni).forEach { (kode, gambar) ->
            hasil.add(DataKartu(kode, gambar))
            hasil.add(DataKartu(kode, gambar))
        }

        hasil.shuffle()
        return hasil
    }

    private fun buatViewKartu(
        data: DataKartu,
        lebarKartu: Int,
        tinggiKartu: Int
    ): FrameLayout {
        val kartu = FrameLayout(this).apply {
            layoutParams = GridLayout.LayoutParams().apply {
                width = lebarKartu
                height = tinggiKartu
                setMargins(dpToPx(5), dpToPx(5), dpToPx(5), dpToPx(5))
            }

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

        kartu.setOnClickListener {
            pilihKartu(kartu)
        }

        return kartu
    }

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

    private fun pilihKartu(kartu: FrameLayout) {
        if (permainanSelesai || sedangPreview || sedangMemeriksa) return
        if (kartu == kartuPertama) return

        val data = kartu.tag as DataKartu
        if (data.terbuka || data.selesai) return

        tampilkanKartuTerbuka(kartu)

        if (kartuPertama == null) {
            kartuPertama = kartu
        } else {
            kartuKedua = kartu
            cekPasangan()
        }
    }

    private fun cekPasangan() {
        sedangMemeriksa = true

        val dataPertama = kartuPertama?.tag as? DataKartu
        val dataKedua = kartuKedua?.tag as? DataKartu
        val pasanganSama = dataPertama?.kode == dataKedua?.kode

        if (pasanganSama) {
            kartuPertama?.let { tampilkanKartuBenar(it) }
            kartuKedua?.let { tampilkanKartuBenar(it) }

            jumlahPasanganSelesai++
            totalBenar++
            skor += 15
            tvSkor.text = skor.toString()

            handler.postDelayed({
                if (permainanSelesai) return@postDelayed

                resetPilihan()

                if (jumlahPasanganSelesai == totalPasanganSesiIni) {
                    prosesSesiSelesai()
                }
            }, 450L)
        } else {
            sesiAdaKesalahan = true
            totalSalah++

            handler.postDelayed({
                if (permainanSelesai) return@postDelayed

                kartuPertama?.let { tampilkanKartuTertutup(it) }
                kartuKedua?.let { tampilkanKartuTertutup(it) }
                resetPilihan()
            }, 750L)
        }
    }

    private fun prosesSesiSelesai() {
        if (permainanSelesai) return

        // Satu sesi dinilai benar hanya jika seluruh pasangan diselesaikan tanpa salah pilih.
        // Dengan begitu, adaptif dihitung per sesi, bukan per kartu yang ditekan.
        val sesiBenar = !sesiAdaKesalahan
        val faseBaru = adaptiveManager.prosesJawaban(sesiBenar)

        if (faseBaru != faseSaatIni) {
            faseSaatIni = faseBaru
            Toast.makeText(this, "Fase berubah ke Fase $faseSaatIni", Toast.LENGTH_SHORT).show()
        }

        tvPetunjuk.text = "Bagus! Bersiap untuk sesi berikutnya..."

        handler.postDelayed({
            if (!permainanSelesai) {
                mulaiSesiBaru()
            }
        }, 900L)
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

    private fun simpanRiwayatAkhir() {
        if (permainanSelesai) return
        permainanSelesai = true

        hentikanProsesBerjalan()
        gridKartu.removeAllViews()

        val totalJawaban = totalBenar + totalSalah
        val akurasi = if (totalJawaban > 0) {
            ((totalBenar * 100f) / totalJawaban).toInt()
        } else {
            0
        }

        val durasiMillis = System.currentTimeMillis() - waktuMulaiPermainan
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

    private fun hentikanProsesBerjalan() {
        timerPermainan?.cancel()
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
