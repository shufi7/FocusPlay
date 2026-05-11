package com.example.focusplay.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 1. Mengenali tombol Masuk berdasarkan ID yang kita buat di XML
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        // 2. Memberikan perintah saat tombol tersebut diklik
        btnLogin.setOnClickListener {
            // Memunculkan pesan kecil (Toast) di bawah layar sebagai indikator
            Toast.makeText(this, "Berhasil Masuk!", Toast.LENGTH_SHORT).show()

            // 3. Berpindah dari LoginActivity ke DashboardActivity
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)

            // Menutup halaman login agar tidak kembali saat tombol back ditekan
            finish()
        }
    }
}