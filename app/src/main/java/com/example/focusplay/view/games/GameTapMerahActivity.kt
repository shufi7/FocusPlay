package com.example.focusplay.view.games

import android.os.Bundle
import android.os.CountDownTimer
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_tap_merah)

        tvSkor = findViewById(R.id.tvSkor)
        tvWaktu = findViewById(R.id.tvWaktu)
        viewTargetMerah = findViewById(R.id.viewTargetMerah)
        layStatus = findViewById(R.id.layStatus)

        // Sensor ketika lingkaran merah berhasil ditekan
        viewTargetMerah.setOnClickListener {
            if (isGameRunning) {
                skor++
                tvSkor.text = "Skor: $skor"
                pindahkanTargetSecaraAcak()
            }
        }

        mulaiPermainan()
    }

    private fun mulaiPermainan() {
        skor = 0
        tvSkor.text = "Skor: 0"
        isGameRunning = true

        // Timer berjalan selama 30 detik (30.000 milidetik)
        timer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tvWaktu.text = "Sisa: ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                isGameRunning = false
                tvWaktu.text = "Waktu Habis!"
                viewTargetMerah.visibility = View.GONE // Hilangkan target saat waktu habis

                // Tampilkan hasil akhir
                Toast.makeText(this@GameTapMerahActivity, "Kerja bagus! Skor kamu: $skor", Toast.LENGTH_LONG).show()
            }
        }.start()
    }

    private fun pindahkanTargetSecaraAcak() {
        // Ambil ukuran layar yang tersedia
        val rootLayout = findViewById<View>(android.R.id.content)

        // Batasi ruang gerak agar lingkaran merah tidak keluar dari tepi layar
        val batasKanan = rootLayout.width - viewTargetMerah.width
        val batasBawah = rootLayout.height - viewTargetMerah.height - layStatus.height

        // Jika layar sudah siap dan ukurannya terbaca
        if (batasKanan > 0 && batasBawah > 0) {
            val XAcak = Random.nextInt(0, batasKanan).toFloat()
            // Ditambah tinggi layStatus agar target tidak tertimpa barisan skor di atas
            val YAcak = Random.nextInt(layStatus.height, batasBawah + layStatus.height).toFloat()

            // Pindahkan posisi target
            viewTargetMerah.x = XAcak
            viewTargetMerah.y = YAcak
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Pastikan timer dimatikan jika anak menutup halaman sebelum waktu habis
        timer?.cancel()
    }
}