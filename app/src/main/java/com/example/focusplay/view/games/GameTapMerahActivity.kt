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
    private lateinit var viewTargetBiru: View
    private lateinit var layStatus: View

    private var skor = 0
    private var timer: CountDownTimer? = null
    private var isGameRunning = false

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var gerakOtomatis: Runnable

    // PENGATURAN KECEPATAN (RAMAH ANAK)
    // Kecepatan awal 1.5 detik
    private var kecepatanLompat: Long = 1500
    // Batas maksimal kecepatan agar masih logis ditekan anak-anak (0.6 detik)
    private val kecepatanMaksimal: Long = 600

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_tap_merah)

        tvSkor = findViewById(R.id.tvSkor)
        tvWaktu = findViewById(R.id.tvWaktu)
        viewTargetMerah = findViewById(R.id.viewTargetMerah)
        viewTargetBiru = findViewById(R.id.viewTargetBiru)
        layStatus = findViewById(R.id.layStatus)

        gerakOtomatis = Runnable {
            if (isGameRunning) {
                pindahkanTargetSecaraAcak()
                handler.postDelayed(gerakOtomatis, kecepatanLompat)
            }
        }

        viewTargetMerah.setOnClickListener {
            if (isGameRunning) {
                skor++
                tvSkor.text = "Skor: $skor"

                // Tingkatkan kecepatan permainan secara halus (30 milidetik saja)
                if (kecepatanLompat > kecepatanMaksimal) {
                    kecepatanLompat -= 30
                }

                handler.removeCallbacks(gerakOtomatis)
                pindahkanTargetSecaraAcak()
                handler.postDelayed(gerakOtomatis, kecepatanLompat)
            }
        }

        viewTargetBiru.setOnClickListener {
            if (isGameRunning) {
                skor--
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
        kecepatanLompat = 1500 // Reset ke kecepatan awal
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

                Toast.makeText(this@GameTapMerahActivity, "Selesai! Skor akhirmu: $skor", Toast.LENGTH_LONG).show()
            }
        }.start()
    }

    private fun pindahkanTargetSecaraAcak() {
        val rootLayout = findViewById<View>(android.R.id.content)
        val batasKanan = rootLayout.width - viewTargetMerah.width
        val batasBawah = rootLayout.height - viewTargetMerah.height - layStatus.height

        if (batasKanan > 0 && batasBawah > 0) {
            val merahX = Random.nextInt(0, batasKanan).toFloat()
            val merahY = Random.nextInt(layStatus.height, batasBawah + layStatus.height).toFloat()

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