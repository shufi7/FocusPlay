package com.example.focusplay.games

import android.content.ClipData
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.DragEvent
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import com.example.focusplay.utils.AdaptiveGameManager
import com.example.focusplay.utils.GameResultHelper
import com.example.focusplay.history.EvaluasiActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.random.Random

class GameAntarRumahActivity : AppCompatActivity() {

    private lateinit var btnKembali: ImageView
    private lateinit var tvFase: TextView
    private lateinit var tvTimer: TextView
    private lateinit var tvSkor: TextView
    private lateinit var arenaGame: FrameLayout

    private lateinit var adaptiveManager: AdaptiveGameManager
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var idAnak: String = ""
    private var namaAnak: String = "Anak"

    private var faseSekarang = 1
    private var modeAdaptif = true
    private var skor = 0
    private var totalBenar = 0
    private var totalSalah = 0
    private var targetWaktuMenit = 10
    private var delayItemMuncul = 1200L

    private var waktuMulaiSesi = 0L
    private var waktuItemMuncul = 0L
    private var totalResponseMillis = 0L

    private var sesiSelesai = false
    private var countDownTimer: CountDownTimer? = null

    private val namaGame = "Antar ke Rumah"

    private val daftarWarna = listOf(
        WarnaGame("Merah", Color.parseColor("#EF4444")),
        WarnaGame("Biru", Color.parseColor("#3B82F6")),
        WarnaGame("Hijau", Color.parseColor("#22C55E")),
        WarnaGame("Kuning", Color.parseColor("#FACC15"))
    )

    data class WarnaGame(
        val nama: String,
        val warna: Int
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_antar_rumah)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        ambilDataAnakDariIntent()
        hubungkanView()
        bacaPengaturanGame()
        aturTombol()

        waktuMulaiSesi = System.currentTimeMillis()

        mulaiTimer()
        terapkanFase(faseSekarang)
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

        val kecepatanItem = prefs.getInt("kecepatan_item", 1)
        targetWaktuMenit = prefs.getString("target_waktu", "10")?.toIntOrNull() ?: 10

        faseSekarang = 1

        delayItemMuncul = when (kecepatanItem) {
            0 -> 2000L
            1 -> 1200L
            else -> 700L
        }

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

        tvTimer.visibility = View.VISIBLE

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

    private fun terapkanFase(fase: Int) {
        if (sesiSelesai) return

        faseSekarang = fase

        tvFase.text = when (faseSekarang) {
            1 -> "Fase 1: Mudah"
            2 -> "Fase 2: Sedang"
            else -> "Fase 3: Sulit"
        }

        arenaGame.removeAllViews()

        val warnaAktif = getWarnaAktifBerdasarkanFase()
        buatRumah(warnaAktif)

        arenaGame.postDelayed({
            if (!sesiSelesai) {
                buatItem(warnaAktif)
            }
        }, delayItemMuncul)
    }

    private fun getWarnaAktifBerdasarkanFase(): List<WarnaGame> {
        return when (faseSekarang) {
            1 -> daftarWarna.take(2)
            2 -> daftarWarna.take(3)
            else -> daftarWarna.take(4)
        }
    }

    private fun buatRumah(warnaAktif: List<WarnaGame>) {
        val jumlah = warnaAktif.size
        val ukuranRumah = 82
        val jarak = 18

        warnaAktif.forEachIndexed { index, warnaGame ->
            val rumah = TextView(this).apply {
                text = "⌂"
                textSize = 38f
                gravity = Gravity.CENTER
                tag = warnaGame.nama
                setTextColor(Color.WHITE)
                typeface = Typeface.DEFAULT_BOLD
                background = roundedDrawable(warnaGame.warna, 18)

                setOnDragListener { targetView, event ->
                    when (event.action) {
                        DragEvent.ACTION_DROP -> {
                            val warnaItem = event.clipDescription.label.toString()
                            val warnaRumah = targetView.tag.toString()
                            cekJawaban(warnaItem, warnaRumah)
                            true
                        }
                        else -> true
                    }
                }
            }

            val params = FrameLayout.LayoutParams(dp(ukuranRumah), dp(ukuranRumah))
            params.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL

            val totalLebar = (jumlah * ukuranRumah) + ((jumlah - 1) * jarak)
            val startX = -totalLebar / 2 + ukuranRumah / 2

            params.leftMargin = dp(startX + index * (ukuranRumah + jarak))
            params.bottomMargin = dp(24)

            arenaGame.addView(rumah, params)
        }
    }

    private fun buatItem(warnaAktif: List<WarnaGame>) {
        val warnaGame = warnaAktif.random()
        waktuItemMuncul = System.currentTimeMillis()

        val item = TextView(this).apply {
            text = "●"
            textSize = 46f
            gravity = Gravity.CENTER
            setTextColor(warnaGame.warna)
            tag = warnaGame.nama
            typeface = Typeface.DEFAULT_BOLD

            setOnTouchListener { view, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    val clipData = ClipData.newPlainText(warnaGame.nama, warnaGame.nama)
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

        val params = FrameLayout.LayoutParams(dp(82), dp(82))

        val batasKanan = maxOf(dp(100), arenaGame.width - dp(110))
        params.leftMargin = Random.nextInt(dp(20), batasKanan)
        params.topMargin = dp(30)

        arenaGame.addView(item, params)
    }

    private fun cekJawaban(warnaItem: String, warnaRumah: String) {
        if (sesiSelesai) return

        val benar = warnaItem == warnaRumah

        val responseMillis = System.currentTimeMillis() - waktuItemMuncul
        totalResponseMillis += responseMillis

        if (benar) {
            skor += 10
            totalBenar++
        } else {
            totalSalah++
        }

        tvSkor.text = "Skor: $skor"

        val faseBaru = adaptiveManager.prosesJawaban(benar)

        if (faseBaru != faseSekarang) {
            terapkanFase(faseBaru)
        } else {
            arenaGame.removeAllViews()
            val warnaAktif = getWarnaAktifBerdasarkanFase()
            buatRumah(warnaAktif)

            arenaGame.postDelayed({
                if (!sesiSelesai) {
                    buatItem(warnaAktif)
                }
            }, delayItemMuncul)
        }
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

    private fun roundedDrawable(color: Int, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(color)
            cornerRadius = dp(radius).toFloat()
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}