package com.example.focusplay.view.games

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.CountDownTimer
import android.view.DragEvent
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import com.example.focusplay.utils.AdaptiveGameManager
import kotlin.random.Random

class GameAntarRumahActivity : AppCompatActivity() {

    private lateinit var btnKembali: ImageView
    private lateinit var tvFase: TextView
    private lateinit var tvTimer: TextView
    private lateinit var tvSkor: TextView
    private lateinit var arenaGame: FrameLayout

    private lateinit var adaptiveManager: AdaptiveGameManager

    private var faseSekarang = 1
    private var modeAdaptif = true
    private var skor = 0
    private var totalBenar = 0
    private var totalSalah = 0
    private var targetWaktuMenit = 10
    private var delayItemMuncul = 1200L

    private var countDownTimer: CountDownTimer? = null

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

        hubungkanView()
        bacaPengaturanGame()
        aturTombol()
        mulaiTimer()
        terapkanFase(faseSekarang)
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

        val faseIndex = prefs.getInt("fase_index", 0)
        modeAdaptif = prefs.getBoolean("mode_adaptif", true)

        val kecepatanItem = prefs.getInt("kecepatan_item", 1)
        targetWaktuMenit = prefs.getString("target_waktu", "10")?.toIntOrNull() ?: 10

        faseSekarang = faseIndex + 1

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
                tvTimer.text = "⏳ ${menit}:${sisaDetik.toString().padStart(2, '0')}"
            }

            override fun onFinish() {
                tvTimer.text = "⏳ 0:00"
                tampilkanHasilSementara()
            }
        }.start()
    }

    private fun terapkanFase(fase: Int) {
        faseSekarang = fase

        tvFase.text = when (faseSekarang) {
            1 -> "Fase 1: Mudah"
            2 -> "Fase 2: Sedang"
            else -> "Fase 3: Sulit"
        }

        arenaGame.removeAllViews()

        val warnaAktif = when (faseSekarang) {
            1 -> daftarWarna.take(2)
            2 -> daftarWarna.take(3)
            else -> daftarWarna.take(4)
        }

        buatRumah(warnaAktif)
        arenaGame.postDelayed({
            buatItem(warnaAktif)
        }, delayItemMuncul)
    }

    private fun buatRumah(warnaAktif: List<WarnaGame>) {
        val jumlah = warnaAktif.size
        val ukuranRumah = 86
        val jarak = 22

        warnaAktif.forEachIndexed { index, warnaGame ->
            val rumah = TextView(this).apply {
                text = "🏠"
                textSize = 34f
                gravity = Gravity.CENTER
                tag = warnaGame.nama
                setBackgroundColor(warnaGame.warna)

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

        val item = TextView(this).apply {
            text = "●"
            textSize = 44f
            gravity = Gravity.CENTER
            setTextColor(warnaGame.warna)
            tag = warnaGame.nama
            typeface = Typeface.DEFAULT_BOLD

            setOnTouchListener { view, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    val shadow = View.DragShadowBuilder(view)
                    view.startDragAndDrop(
                        android.content.ClipData.newPlainText(warnaGame.nama, warnaGame.nama),
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

        val params = FrameLayout.LayoutParams(dp(80), dp(80))
        params.leftMargin = Random.nextInt(20, maxOf(21, arenaGame.width - dp(100)))
        params.topMargin = dp(30)

        arenaGame.addView(item, params)
    }

    private fun cekJawaban(warnaItem: String, warnaRumah: String) {
        val benar = warnaItem == warnaRumah

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
            val warnaAktif = when (faseSekarang) {
                1 -> daftarWarna.take(2)
                2 -> daftarWarna.take(3)
                else -> daftarWarna.take(4)
            }
            buatRumah(warnaAktif)
            arenaGame.postDelayed({
                buatItem(warnaAktif)
            }, delayItemMuncul)
        }
    }

    private fun tampilkanHasilSementara() {
        arenaGame.removeAllViews()

        val totalJawaban = totalBenar + totalSalah
        val akurasi = if (totalJawaban > 0) {
            (totalBenar * 100) / totalJawaban
        } else {
            0
        }

        val hasil = TextView(this).apply {
            text = "Sesi selesai!\n\nSkor: $skor\nBenar: $totalBenar\nSalah: $totalSalah\nAkurasi: $akurasi%"
            textSize = 20f
            gravity = Gravity.CENTER
            setTextColor(Color.parseColor("#1F2937"))
            typeface = Typeface.DEFAULT_BOLD
        }

        arenaGame.addView(
            hasil,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}