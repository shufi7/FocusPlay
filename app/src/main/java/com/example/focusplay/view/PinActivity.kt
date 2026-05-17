package com.example.focusplay.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import com.example.focusplay.utils.SessionManager

class PinActivity : AppCompatActivity() {

    private lateinit var tvJudulPin: TextView
    private lateinit var tvSubJudulPin: TextView
    private lateinit var etPin: EditText
    private lateinit var btnSubmitPin: Button

    private lateinit var session: SessionManager
    private var isModeBuatPin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)

        tvJudulPin = findViewById(R.id.tvJudulPin)
        tvSubJudulPin = findViewById(R.id.tvSubJudulPin)
        etPin = findViewById(R.id.etPin)
        btnSubmitPin = findViewById(R.id.btnSubmitPin)

        session = SessionManager(this)

        val pinTersimpan = session.getPin()

        // Cek apakah PIN sudah pernah dibuat sebelumnya
        if (pinTersimpan.isEmpty()) {
            isModeBuatPin = true
            tvJudulPin.text = "Buat PIN Baru"
            tvSubJudulPin.text = "Buat 4 digit PIN untuk mengamankan area Orang Tua agar tidak bisa diakses anak."
            btnSubmitPin.text = "Simpan PIN"
        } else {
            isModeBuatPin = false
            tvJudulPin.text = "Masukkan PIN"
            tvSubJudulPin.text = "Masukkan 4 digit PIN rahasia untuk masuk ke Dasbor Orang Tua."
            btnSubmitPin.text = "Masuk Dasbor"
        }

        btnSubmitPin.setOnClickListener {
            prosesPin(pinTersimpan)
        }
    }

    private fun prosesPin(pinTersimpan: String) {
        val inputPin = etPin.text.toString().trim()

        if (inputPin.length < 4) {
            Toast.makeText(this, "PIN harus 4 digit!", Toast.LENGTH_SHORT).show()
            return
        }

        if (isModeBuatPin) {
            // Mode menyimpan PIN baru
            session.simpanPin(inputPin)
            Toast.makeText(this, "PIN berhasil dibuat!", Toast.LENGTH_SHORT).show()
            lanjutKeDasborOrtu()
        } else {
            // Mode verifikasi PIN lama
            if (inputPin == pinTersimpan) {
                lanjutKeDasborOrtu()
            } else {
                Toast.makeText(this, "PIN Salah! Coba lagi.", Toast.LENGTH_SHORT).show()
                etPin.text.clear() // Kosongkan kolom jika salah
            }
        }
    }

    private fun lanjutKeDasborOrtu() {
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
        finish()
    }
}