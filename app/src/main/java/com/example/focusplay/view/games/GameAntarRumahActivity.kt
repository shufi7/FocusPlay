package com.example.focusplay.view.games

import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import kotlin.random.Random

class GameAntarRumahActivity : AppCompatActivity() {

    private lateinit var arenaGame: FrameLayout
    private lateinit var tvSkor: TextView
    private lateinit var tvFase: TextView
    private lateinit var tvTimer: TextView

    private var skor = 0
    private var faseSaatIni = 1
    private var idAnak = ""

    private var jumlahObjekSelesai = 0
    private var targetSelesaiRondeIni = 0

    private val warnaTersedia = listOf(
        "#F44336", // Merah
        "#2196F3", // Biru
        "#4CAF50", // Hijau
        "#FF9800", // Oranye
        "#9C27B0"  // Ungu
    )

    private var timerFase3: CountDownTimer? = null
    private var waktuHabis = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_antar_rumah)

        idAnak = intent.getStringExtra("ID_ANAK") ?: ""

        arenaGame = findViewById(R.id.arenaGame)
        tvSkor = findViewById(R.id.tvSkor)
        tvFase = findViewById(R.id.tvFase)
        tvTimer = findViewById(R.id.tvTimer)

        findViewById<ImageView>(R.id.btnKembali).setOnClickListener {
            finish()
        }

        arenaGame.post {
            mulaiRonde()
        }
    }

    private fun mulaiRonde() {
        arenaGame.removeAllViews()
        jumlahObjekSelesai = 0
        waktuHabis = false
        timerFase3?.cancel()

        val jumlahRumah: Int
        val jumlahObjek: Int

        when (faseSaatIni) {
            1 -> {
                tvFase.text = "Fase 1: Pengenalan"
                tvTimer.visibility = View.GONE
                jumlahRumah = 1
                jumlahObjek = 1
            }
            2 -> {
                tvFase.text = "Fase 2: Target Ganda"
                tvTimer.visibility = View.GONE
                jumlahRumah = 2
                jumlahObjek = 2
            }
            else -> {
                tvFase.text = "Fase 3: Kecepatan & Pengecoh"
                tvTimer.visibility = View.VISIBLE
                jumlahRumah = 3 // 1 Pengecoh
                jumlahObjek = 2
                mulaiTimer(15000) // 15 Detik
            }
        }

        targetSelesaiRondeIni = jumlahObjek

        // Pilih warna acak untuk ronde ini
        val warnaRondeIni = warnaTersedia.shuffled().take(jumlahRumah)

        // 1. Buat Rumah (Di bagian atas arena)
        val lebarArena = arenaGame.width
        val jarakX = lebarArena / (jumlahRumah + 1)
        val listRumah = mutableListOf<View>()

        for (i in 0 until jumlahRumah) {
            val rumah = buatKotakRumah(warnaRondeIni[i])
            val ukuranPx = dpToPx(80)

            // Atur posisi rumah berjajar di atas
            rumah.x = (jarakX * (i + 1) - (ukuranPx / 2)).toFloat()
            rumah.y = dpToPx(40).toFloat()

            // Simpan warna di tag untuk dicocokkan nanti
            rumah.tag = warnaRondeIni[i]
            arenaGame.addView(rumah)
            listRumah.add(rumah)
        }

        // 2. Buat Objek yang bisa diseret (Di bagian bawah arena)
        // Objek hanya dibuat untuk warna yang BUKAN pengecoh (Fase 3 ada 1 pengecoh)
        val warnaObjek = warnaRondeIni.take(jumlahObjek).shuffled()

        for (i in 0 until jumlahObjek) {
            val objek = buatLingkaranObjek(warnaObjek[i])
            val ukuranPx = dpToPx(60)

            // Atur posisi awal objek berjajar di bawah
            val jarakObjekX = lebarArena / (jumlahObjek + 1)
            val posisiAwalX = (jarakObjekX * (i + 1) - (ukuranPx / 2)).toFloat()
            val posisiAwalY = arenaGame.height - dpToPx(120).toFloat()

            objek.x = posisiAwalX
            objek.y = posisiAwalY
            objek.tag = warnaObjek[i]

            pasangFiturSeret(objek, posisiAwalX, posisiAwalY, listRumah)
            arenaGame.addView(objek)
        }
    }

    private fun buatKotakRumah(warnaHex: String): TextView {
        val ukuranPx = dpToPx(80)
        val view = TextView(this).apply {
            layoutParams = FrameLayout.LayoutParams(ukuranPx, ukuranPx)
            text = "🏠"
            textSize = 32f
            gravity = Gravity.CENTER

            val kotakBg = GradientDrawable()
            kotakBg.shape = GradientDrawable.RECTANGLE
            kotakBg.cornerRadius = 16f
            kotakBg.setColor(Color.parseColor(warnaHex))
            background = kotakBg
            elevation = 2f
        }
        return view
    }

    private fun buatLingkaranObjek(warnaHex: String): View {
        val ukuranPx = dpToPx(60)
        val view = View(this).apply {
            layoutParams = FrameLayout.LayoutParams(ukuranPx, ukuranPx)
            val bulatBg = GradientDrawable()
            bulatBg.shape = GradientDrawable.OVAL
            bulatBg.setColor(Color.parseColor(warnaHex))
            background = bulatBg
            elevation = 6f // Objek selalu melayang di atas rumah
        }
        return view
    }

    private fun pasangFiturSeret(objek: View, awalX: Float, awalY: Float, listRumah: List<View>) {
        var dX = 0f
        var dY = 0f

        objek.setOnTouchListener { v, event ->
            if (waktuHabis) return@setOnTouchListener false

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = v.x - event.rawX
                    dY = v.y - event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    v.x = event.rawX + dX
                    v.y = event.rawY + dY
                }
                MotionEvent.ACTION_UP -> {
                    // Cek apakah objek dilepas di atas rumah yang warnanya sama
                    var diletakkanBenar = false

                    for (rumah in listRumah) {
                        if (isOverlap(v, rumah)) {
                            if (v.tag == rumah.tag) {
                                // Benar! Warnanya cocok
                                diletakkanBenar = true
                                v.x = rumah.x + dpToPx(10) // Paskan posisi
                                v.y = rumah.y + dpToPx(10)
                                v.setOnTouchListener(null) // Kunci objek agar tidak bisa diseret lagi

                                objekBerhasilMasuk()
                            } else {
                                Toast.makeText(this@GameAntarRumahActivity, "Warnanya beda!", Toast.LENGTH_SHORT).show()
                            }
                            break
                        }
                    }

                    // Jika salah tempat, kembalikan ke posisi awal animasi memantul
                    if (!diletakkanBenar) {
                        v.animate().x(awalX).y(awalY).setDuration(300).start()
                    }
                }
            }
            true
        }
    }

    private fun isOverlap(view1: View, view2: View): Boolean {
        val rect1 = Rect()
        view1.getHitRect(rect1)
        val rect2 = Rect()
        view2.getHitRect(rect2)
        return Rect.intersects(rect1, rect2)
    }

    private fun objekBerhasilMasuk() {
        jumlahObjekSelesai++
        if (jumlahObjekSelesai == targetSelesaiRondeIni) {
            skor += 20
            tvSkor.text = "Skor: $skor"
            cekNaikFase()
        }
    }

    private fun cekNaikFase() {
        if (skor == 60 && faseSaatIni == 1) {
            faseSaatIni = 2
            Toast.makeText(this, "Mantap! Lanjut ke Fase 2", Toast.LENGTH_SHORT).show()
        } else if (skor == 140 && faseSaatIni == 2) {
            faseSaatIni = 3
            Toast.makeText(this, "Fokus Hebat! Lanjut ke Fase 3", Toast.LENGTH_SHORT).show()
        } else if (skor >= 240) {
            Toast.makeText(this, "Sempurna! Permainan Selesai.", Toast.LENGTH_LONG).show()
            timerFase3?.cancel()
            simpanRiwayatAkhir("Antar Ke Rumah")
            finish()
            return
        }
        mulaiRonde()
    }

    private fun mulaiTimer(durasiMillis: Long) {
        timerFase3 = object : CountDownTimer(durasiMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val detik = millisUntilFinished / 1000
                tvTimer.text = "⏳ ${detik}s"
            }

            override fun onFinish() {
                waktuHabis = true
                tvTimer.text = "Habis!"
                Toast.makeText(this@GameAntarRumahActivity, "Waktu Habis! Coba lagi.", Toast.LENGTH_SHORT).show()
                // Ulangi ronde tanpa menambah skor
                mulaiRonde()
            }
        }.start()
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
        timerFase3?.cancel() // Mencegah memori bocor
    }

    private fun simpanRiwayatAkhir(namaGame: String) {
        val nama = intent.getStringExtra("NAMA_ANAK") ?: "Anak"
        // Simulasi kalkulasi: semakin tinggi skor, akurasi dianggap 100%
        val akurasiSimulasi = if (skor >= 100) 100 else 80

        com.example.focusplay.utils.GameResultHelper.simpanHasilPermainan(
            idAnak = idAnak,
            namaAnak = nama,
            namaGame = namaGame,
            skor = skor,
            akurasi = akurasiSimulasi,
            durasiMenit = 2 // Simulasi durasi bermain 2 menit
        )
    }
}