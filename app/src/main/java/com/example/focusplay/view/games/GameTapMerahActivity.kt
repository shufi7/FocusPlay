package com.example.focusplay.view.games

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import kotlin.random.Random

class GameTapMerahActivity : AppCompatActivity() {

    private lateinit var tvSkor: TextView
    private lateinit var tvWaktu: TextView
    private lateinit var viewTargetMerah: View
    private lateinit var viewTargetBiru: View // Objek Pengecoh
    private lateinit var layStatus: View

    private var skor = 0
    private var timer: CountDownTimer? = null
    private var isGameRunning = false

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var gerakOtomatis: Runnable

    // Kecepatan awal 1.2 detik
    private var kecepatanLompat: Long = 1200
    // Batas maksimal kecepatan agar masih bisa ditekan manusia (0.5 detik)
    private val kecepatanMaksimal: Long = 500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_tap_merah)

        tvSkor = findViewById(R.id.tvSkor)
        tvWaktu = findViewById(R.id.tvWaktu)
        viewTargetMerah = findViewById(R.id.viewTargetMerah)
        viewTargetBiru = findViewById(R.id.viewTargetBiru)
        layStatus = findViewById(R.id.layStatus)

        // Mesin penggerak untuk KEDUA target
        gerakOtomatis = Runnable {
            if (isGameRunning) {
                pindahkanTargetSecaraAcak()
                handler.postDelayed(gerakOtomatis, kecepatanLompat)
            }
        }

        // Jika MERAH ditekan (BENAR)
        viewTargetMerah.setOnClickListener {
            if (isGameRunning) {
                skor++
                tvSkor.text = "Skor: $skor"

                // Tingkatkan kecepatan permainan secara progresif tiap kali berhasil
                if (kecepatanLompat > kecepatanMaksimal) {
                    kecepatanLompat -= 50 // Kurangi jeda 50 milidetik
                }

                handler.removeCallbacks(gerakOtomatis)
                pindahkanTargetSecaraAcak()
                handler.postDelayed(gerakOtomatis, kecepatanLompat)
            }
        }

        // Jika BIRU ditekan (SALAH / TERKECOH)
        viewTargetBiru.setOnClickListener {
            if (isGameRunning) {
                skor-- // Kurangi skor
                tvSkor.text = "Skor: $skor"

                handler.removeCallbacks(gerakOtomatis)
                pindahkanTargetSecaraAcak()
                handler.postDelayed(gerakOtomatis, kecepatanLompat)
            }
        }

        mulaiPermainan()
    }

    private fun mulaiPermainan() {
        skor = 0
        kecepatanLompat = 1200 // Reset kecepatan
        tvSkor.text = "Skor: 0"
        isGameRunning = true

        handler.postDelayed(gerakOtomatis, kecepatanLompat)

        timer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tvWaktu.text = "Sisa: ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                isGameRunning = false
                tvWaktu.text = "Waktu Habis!"
                viewTargetMerah.visibility = View.GONE
                viewTargetBiru.visibility = View.GONE

                handler.removeCallbacks(gerakOtomatis)

                Toast.makeText(this@GameTapMerahActivity, "Selesai! Akurasi refleksmu mencetak Skor: $skor", Toast.LENGTH_LONG).show()
            }
        }.start()
    }

    private fun pindahkanTargetSecaraAcak() {
        val rootLayout = findViewById<View>(android.R.id.content)
        val batasKanan = rootLayout.width - viewTargetMerah.width
        val batasBawah = rootLayout.height - viewTargetMerah.height - layStatus.height

        if (batasKanan > 0 && batasBawah > 0) {
            // Posisi untuk Merah
            val merahX = Random.nextInt(0, batasKanan).toFloat()
            val merahY = Random.nextInt(layStatus.height, batasBawah + layStatus.height).toFloat()

            // Posisi untuk Biru (Pengecoh)
            val biruX = Random.nextInt(0, batasKanan).toFloat()
            val biruY = Random.nextInt(layStatus.height, batasBawah + layStatus.height).toFloat()

            viewTargetMerah.x = merahX
            viewTargetMerah.y = merahY

            viewTargetBiru.x = biruX
            viewTargetBiru.y = biruY
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        handler.removeCallbacks(gerakOtomatis)
    }
}