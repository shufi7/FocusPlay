package com.example.focusplay.view

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R

class RegisterActivity : AppCompatActivity() {

    // Deklarasi variabel untuk elemen layar pendaftaran
    private lateinit var etNama: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnDaftar: Button
    private lateinit var tvMasukSini: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Hubungkan variabel dengan ID di XML
        etNama = findViewById(R.id.etNamaRegister)
        etEmail = findViewById(R.id.etEmailRegister)
        etPassword = findViewById(R.id.etPasswordRegister)
        btnDaftar = findViewById(R.id.btnProsesRegister)
        tvMasukSini = findViewById(R.id.tvMasukSini)

        // Fitur klik tulisan "Masuk di sini"
        tvMasukSini.setOnClickListener {
            finish() // Menutup halaman pendaftaran, otomatis kembali ke layar Login
        }
    }
}