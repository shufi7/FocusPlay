package com.example.focusplay.view

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import com.example.focusplay.model.RegisterResponse
import com.example.focusplay.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    // Deklarasi variabel
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
            finish() // Kembali ke layar Login
        }

        // Beri perintah ketika tombol "Daftar Sekarang" diklik
        btnDaftar.setOnClickListener {
            val nama = etNama.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Cek apakah ada kolom yang kosong
            if (nama.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Semua kolom data wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Jalankan fungsi register ke server
            prosesRegister(nama, email, password)
        }
    }

    private fun prosesRegister(nama: String, email: String, pass: String) {
        // Siapkan data JSON yang akan dikirim
        val requestData = HashMap<String, String>()
        requestData["nama"] = nama
        requestData["email"] = email
        requestData["password"] = pass

        // Panggil API Register lewat Retrofit
        ApiClient.instance.registerPendamping(requestData).enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                if (response.isSuccessful) {
                    // Munculkan notifikasi dan langsung kembalikan pengguna ke layar login
                    Toast.makeText(this@RegisterActivity, "Pendaftaran berhasil! Silakan masuk.", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this@RegisterActivity, "Pendaftaran gagal, email mungkin sudah dipakai.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                // Jika koneksi internet atau server mati
                Toast.makeText(this@RegisterActivity, "Koneksi Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}