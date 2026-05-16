package com.example.focusplay.view

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import java.util.Locale

class DashboardAnakActivity : AppCompatActivity() {

    private lateinit var tvTimer: TextView
    private lateinit var btnMulaiFokus: Button
    private lateinit var btnResetFokus: Button

    private var countDownTimer: CountDownTimer? = null
    private var timerRunning = false

    // Setel waktu ke 25 Menit (dalam milidetik)
    private val START_TIME_IN_MILLIS: Long = 1500000
    private var timeLeftInMillis: Long = START_TIME_IN_MILLIS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_anak)

        val tvWelcomeAnak = findViewById<TextView>(R.id.tvWelcomeAnak)
        val btnKembaliKeOrtu = findViewById<Button>(R.id.btnKembaliKeOrtu)

        tvTimer = findViewById(R.id.tvTimer)
        btnMulaiFokus = findViewById(R.id.btnMulaiFokus)
        btnResetFokus = findViewById(R.id.btnResetFokus)

        // Tangkap sapaan nama anak
        val namaAnak = intent.getStringExtra("NAMA_ANAK") ?: "Anak Hebat"
        tvWelcomeAnak.text = "Halo, $namaAnak!"

        btnMulaiFokus.setOnClickListener {
            if (timerRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        btnResetFokus.setOnClickListener {
            resetTimer()
        }

        btnKembaliKeOrtu.setOnClickListener {
            finish()
        }

        updateCountDownText()
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountDownText()
            }

            override fun onFinish() {
                timerRunning = false
                btnMulaiFokus.text = "Mulai Fokus"
                // Nanti kita bisa tambahkan suara atau notifikasi "Selesai" di sini
            }
        }.start()

        timerRunning = true
        btnMulaiFokus.text = "Jeda (Pause)"
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        timerRunning = false
        btnMulaiFokus.text = "Lanjutkan"
    }

    private fun resetTimer() {
        countDownTimer?.cancel()
        timerRunning = false
        timeLeftInMillis = START_TIME_IN_MILLIS
        updateCountDownText()
        btnMulaiFokus.text = "Mulai Fokus"
    }

    private fun updateCountDownText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        val timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        tvTimer.text = timeLeftFormatted
    }
}