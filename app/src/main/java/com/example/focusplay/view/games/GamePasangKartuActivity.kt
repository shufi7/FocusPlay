package com.example.focusplay.view.games

import android.graphics.Color
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

class GamePasangKartuActivity : AppCompatActivity() {

    private lateinit var gridKartu: GridLayout
    private lateinit var tvSkor: TextView
    private lateinit var tvFase: TextView
    private lateinit var tvTimer: TextView

    private var skor = 0
    private var faseSaatIni = 1
    private var idAnak = ""

    private var jumlahPasanganSelesai = 0
    private var totalPasanganRondeIni = 0

    private var kartuPertama: TextView? = null
    private var kartuKedua: TextView? = null
    private var sedangMemeriksa = false // Mencegah klik liar saat animasi

    private var timerPermainan: CountDownTimer? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_pasang_kartu)

        idAnak = intent.getStringExtra("ID_ANAK") ?: ""

        gridKartu = findViewById(R.id.gridKartu)
        tvSkor = findViewById(R.id.tvSkor)
        tvFase = findViewById(R.id.tvFase)
        tvTimer = findViewById(R.id.tvTimer)

        findViewById<ImageView>(R.id.btnKembali).setOnClickListener {
            finish()
        }

        mulaiRonde()
    }

    private fun mulaiRonde() {
        gridKartu.removeAllViews()
        jumlahPasanganSelesai = 0
        kartuPertama = null
        kartuKedua = null
        sedangMemeriksa = false
        timerPermainan?.cancel()

        val isiKartu = mutableListOf<String>()
        val waktuPreview: Long

        when (faseSaatIni) {
            1 -> {
                tvFase.text = "Fase 1: Pengenalan"
                tvTimer.visibility = View.GONE
                gridKartu.columnCount = 2
                gridKartu.rowCount = 2
                totalPasanganRondeIni = 2
                waktuPreview = 3000L // 3 Detik lihat kartu

                // Ikon beda jauh
                isiKartu.addAll(listOf("🍎", "🍎", "🐶", "🐶"))
            }
            2 -> {
                tvFase.text = "Fase 2: Memori Bertambah"
                tvTimer.visibility = View.GONE
                gridKartu.columnCount = 3
                gridKartu.rowCount = 3 // Akan ada 1 tempat kosong tapi aman untuk 8 kartu
                totalPasanganRondeIni = 4
                waktuPreview = 2000L // 2 Detik lihat kartu

                // 4 Pasang
                isiKartu.addAll(listOf("🍎", "🍎", "🐶", "🐶", "🚗", "🚗", "⚽", "⚽"))
            }
            else -> {
                tvFase.text = "Fase 3: Tantangan Mirip!"
                tvTimer.visibility = View.VISIBLE
                gridKartu.columnCount = 3
                gridKartu.rowCount = 4
                totalPasanganRondeIni = 6
                waktuPreview = 1000L // Cuma 1 Detik!

                // Ikon mirip sebagai pengecoh (Apel vs Tomat, Anjing vs Serigala)
                isiKartu.addAll(listOf("🍎", "🍎", "🍅", "🍅", "🐶", "🐶", "🐺", "🐺", "🚗", "🚗", "🚙", "🚙"))
                mulaiTimer(45000) // Waktu mencari: 45 detik
            }
        }

        isiKartu.shuffle() // Acak posisi

        // Buat kartu secara dinamis
        for (i in 0 until (totalPasanganRondeIni * 2)) {
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
                tag = isiKartu[i] // Simpan isi sebenarnya di tag

                // Tampilan Awal: Tampilkan isi (Preview)
                text = isiKartu[i]
                background = buatBackgroundKartu("#FFFFFF", "#E0E0E0")
            }
            gridKartu.addView(kartu)
        }

        // Tutup kartu setelah waktu preview habis
        handler.postDelayed({
            for (i in 0 until gridKartu.childCount) {
                val kartu = gridKartu.getChildAt(i) as TextView
                tutupKartu(kartu)

                // Aktifkan fitur klik
                kartu.setOnClickListener { klikKartu(kartu) }
            }
        }, waktuPreview)
    }

    private fun klikKartu(kartuDipilih: TextView) {
        // Jangan lakukan apa-apa jika kartu sedang animasi, kartu sudah terbuka, atau permainan dikunci
        if (sedangMemeriksa || kartuDipilih.text.isNotEmpty()) return

        bukaKartu(kartuDipilih)

        if (kartuPertama == null) {
            kartuPertama = kartuDipilih
        } else {
            kartuKedua = kartuDipilih
            sedangMemeriksa = true
            cekKecocokan()
        }
    }

    private fun cekKecocokan() {
        if (kartuPertama?.tag == kartuKedua?.tag) {
            // Cocok!
            kartuPertama?.setBackgroundColor(Color.parseColor("#C8E6C9")) // Hijau
            kartuKedua?.setBackgroundColor(Color.parseColor("#C8E6C9"))

            // Kunci kartu
            kartuPertama?.setOnClickListener(null)
            kartuKedua?.setOnClickListener(null)

            jumlahPasanganSelesai++
            skor += 15
            tvSkor.text = "Skor: $skor"

            resetPilihan()

            if (jumlahPasanganSelesai == totalPasanganRondeIni) {
                handler.postDelayed({ cekNaikFase() }, 500)
            }

        } else {
            // Tidak Cocok, tutup kembali setelah setengah detik
            handler.postDelayed({
                kartuPertama?.let { tutupKartu(it) }
                kartuKedua?.let { tutupKartu(it) }
                resetPilihan()
            }, 600)
        }
    }

    private fun resetPilihan() {
        kartuPertama = null
        kartuKedua = null
        sedangMemeriksa = false
    }

    private fun bukaKartu(kartu: TextView) {
        kartu.text = kartu.tag.toString()
        kartu.background = buatBackgroundKartu("#FFFFFF", "#2196F3") // Border Biru
    }

    private fun tutupKartu(kartu: TextView) {
        kartu.text = ""
        kartu.background = buatBackgroundKartu("#2196F3", "#1565C0") // Punggung Kartu Biru
    }

    private fun buatBackgroundKartu(warnaBg: String, warnaBorder: String): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 16f
            setColor(Color.parseColor(warnaBg))
            setStroke(4, Color.parseColor(warnaBorder))
        }
    }

    private fun cekNaikFase() {
        if (faseSaatIni == 1) {
            faseSaatIni = 2
            Toast.makeText(this, "Ingatan Bagus! Lanjut ke Fase 2", Toast.LENGTH_SHORT).show()
        } else if (faseSaatIni == 2) {
            faseSaatIni = 3
            Toast.makeText(this, "Super! Hati-hati jebakan di Fase 3", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Sempurna! Kamu memiliki ingatan fotografis!", Toast.LENGTH_LONG).show()
            timerPermainan?.cancel()
            simpanRiwayatAkhir("Pasang Kartu")
            finish()
            return
        }
        mulaiRonde()
    }

    private fun mulaiTimer(durasiMillis: Long) {
        timerPermainan = object : CountDownTimer(durasiMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val detik = millisUntilFinished / 1000
                tvTimer.text = "⏳ ${detik}s"
            }

            override fun onFinish() {
                tvTimer.text = "Habis!"
                Toast.makeText(this@GamePasangKartuActivity, "Waktu Habis! Coba lagi.", Toast.LENGTH_SHORT).show()
                mulaiRonde() // Restart ronde
            }
        }.start()
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
        timerPermainan?.cancel()
        handler.removeCallbacksAndMessages(null) // Hapus antrean animasi
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
                // LOMPAT KE HALAMAN EVALUASI
                val intentToEvaluasi = android.content.Intent(this, com.example.focusplay.view.EvaluasiActivity::class.java)
                intentToEvaluasi.putExtra("ID_ANAK", idAnak)
                intentToEvaluasi.putExtra("NAMA_ANAK", nama)
                intentToEvaluasi.putExtra("EVALUASI_LANGSUNG", hasilEvaluasi)
                startActivity(intentToEvaluasi)

                // Tutup game setelah melompat
                finish()
            }
        )
        // 🚨 SANGAT PENTING: JANGAN ADA TULISAN finish() SAMA SEKALI DI AREA SINI!
    }
}