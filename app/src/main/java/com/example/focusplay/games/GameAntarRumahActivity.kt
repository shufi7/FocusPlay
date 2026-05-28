package com.example.focusplay.games

import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.DragEvent
import android.view.MotionEvent
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

class GameAntarRumahActivity : AppCompatActivity() {

    private lateinit var btnKembali: ImageView
    private lateinit var tvFase: TextView
    private lateinit var tvTimer: TextView
    private lateinit var tvSkor: TextView
    private lateinit var arenaGame: FrameLayout
    private lateinit var adaptiveManager: AdaptiveGameManager

    private var idAnak = ""
    private var namaAnak = "Anak"

    private var faseSekarang = 1
    private var modeAdaptif = true
    private var skor = 0
    private var totalBenar = 0
    private var totalSalah = 0
    private var targetWaktuMenit = 1

    private var waktuMulaiSesi = 0L
    private var sesiSelesai = false
    private var countDownTimer: CountDownTimer? = null

    private val namaGame = "Antar ke Rumah"

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
        setContentView(R.layout.activity_game_antar_rumah)

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
    }

    private fun hubungkanView() {
        btnKembali = findViewById(R.id.btnKembali)
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
        btnKembali.setOnClickListener {
            countDownTimer?.cancel()
            finish()
        }
    }

    private fun mulaiTimer() {
        val totalMillis = targetWaktuMenit * 60 * 1000L

        countDownTimer = object : CountDownTimer(totalMillis, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val detik = millisUntilFinished / 1000
                val menit = detik / 60
                val sisaDetik = detik % 60
                tvTimer.text = "${menit}:${sisaDetik.toString().padStart(2, '0')}"
            }

            override fun onFinish() {
                tvTimer.text = "0:00"
                selesaikanSesiDanSimpan()
            }
        }.start()
    }

    private fun mulaiRonde() {
        if (sesiSelesai) return

        arenaGame.removeAllViews()

        tvFase.text = "Fase $faseSekarang"
        tvSkor.text = "Skor: $skor"

        val itemAktif = when (faseSekarang) {
            1 -> daftarDombaRumah.take(2)
            2 -> daftarDombaRumah.take(3)
            else -> daftarDombaRumah.take(4)
        }

        arenaGame.post {
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
            params.topMargin = (arenaGame.height * 0.41f).toInt()

            arenaGame.addView(rumah, params)
        }
    }

    private fun tampilkanDomba(itemAktif: List<DombaRumah>) {
        val data = itemAktif.random()

        val domba = ImageView(this).apply {
            setImageResource(data.gambarDomba)
            scaleType = ImageView.ScaleType.FIT_CENTER
            tag = data.nama

            setOnTouchListener { view, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    val clipData = ClipData.newPlainText(data.nama, data.nama)
                    val shadow = View.DragShadowBuilder(view)

                    view.startDragAndDrop(
                        clipData,
                        shadow,
                        view,
                        0
                    )

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
        if (sesiSelesai) return

        val benar = warnaDomba == warnaRumah

        if (benar) {
            skor += 10
            totalBenar++
        } else {
            totalSalah++
        }

        tvSkor.text = "Skor: $skor"

        val faseBaru = adaptiveManager.prosesJawaban(benar)

        if (faseBaru != faseSekarang) {
            faseSekarang = faseBaru
        }

        mulaiRonde()
    }

    private fun selesaikanSesiDanSimpan() {
        if (sesiSelesai) return
        sesiSelesai = true

        countDownTimer?.cancel()
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
            durasiMenit = durasiMenit
        ) { hasilAI ->
            val intent = Intent(this, EvaluasiActivity::class.java)
            intent.putExtra("ID_ANAK", idAnak)
            intent.putExtra("NAMA_ANAK", namaAnak)
            intent.putExtra("EVALUASI_LANGSUNG", hasilAI)
            startActivity(intent)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}