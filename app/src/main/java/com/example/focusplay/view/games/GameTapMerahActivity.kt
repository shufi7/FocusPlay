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
    private lateinit var layStatus: View

    private var skor = 0
    private var timer: CountDownTimer? = null
    private var isGameRunning = false

    // Mesin penggerak otomatis (Fase 2)
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var gerakOtomatis: Runnable
    private var kecepatanLompat: Long = 1200 // Pindah otomatis tiap 1.2 detik jika tidak ditekan

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_tap_merah)

        tvSkor = findViewById(R.id.tvSkor)
        tvWaktu = findViewById(R.id.tvWaktu)
        viewTargetMerah = findViewById(R.id.viewTargetMerah)
        layStatus = findViewById(R.id.layStatus)

        // Setup pergerakan otomatis
        gerakOtomatis = Runnable {
            if (isGameRunning) {
                pindahkanTargetSecaraAcak()
                // Jadwalkan lompatan berikutnya
                handler.postDelayed(gerakOtomatis, kecepatanLompat)
            }
        }

        // Sensor ketika lingkaran merah berhasil ditekan
        viewTargetMerah.setOnClickListener {
            if (isGameRunning) {
                skor++
                tvSkor.text = "Skor: $skor"

                // Hapus jadwal lompat otomatis sebelumnya, lalu buat jadwal baru dari awal
                handler.removeCallbacks(gerakOtomatis)
                pindahkanTargetSecaraAcak()
                handler.postDelayed(gerakOtomatis, kecepatanLompat)
            }
        }

        mulaiPermainan()
    }

    private fun mulaiPermainan() {
        skor = 0
        tvSkor.text = "Skor: 0"
        isGameRunning = true

        // Mulai pergerakan otomatis saat game dimulai
        handler.postDelayed(gerakOtomatis, kecepatanLompat)

        // Timer game 30 detik
        timer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tvWaktu.text = "Sisa: ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                isGameRunning = false
                tvWaktu.text = "Waktu Habis!"
                viewTargetMerah.visibility = View.GONE

                // Hentikan gerakan otomatis agar tidak error saat waktu habis
                handler.removeCallbacks(gerakOtomatis)

                Toast.makeText(this@GameTapMerahActivity, "Kerja bagus! Skor kamu: $skor", Toast.LENGTH_LONG).show()
            }
        }.start()
    }

    private fun pindahkanTargetSecaraAcak() {
        val rootLayout = findViewById<View>(android.R.id.content)
        val batasKanan = rootLayout.width - viewTargetMerah.width
        val batasBawah = rootLayout.height - viewTargetMerah.height - layStatus.height

        if (batasKanan > 0 && batasBawah > 0) {
            val XAcak = Random.nextInt(0, batasKanan).toFloat()
            val YAcak = Random.nextInt(layStatus.height, batasBawah + layStatus.height).toFloat()

            viewTargetMerah.x = XAcak
            viewTargetMerah.y = YAcak
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Pastikan semua mesin dimatikan jika anak menutup halaman
        timer?.cancel()
        handler.removeCallbacks(gerakOtomatis)
    }
}