package com.example.focusplay.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import com.example.focusplay.model.LoginResponse
import com.example.focusplay.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    // Deklarasi variabel untuk elemen layar
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button // <-- Variabel tombol daftar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Hubungkan variabel dengan ID di XML
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister) // <-- Menghubungkan ID tombol daftar

        // Beri perintah ketika tombol "Masuk" diklik
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Cek apakah kolom kosong
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Jalankan fungsi login jika tidak kosong
            prosesLogin(email, password)
        }

        // Beri perintah ketika tombol "Daftar Baru" diklik
        btnRegister.setOnClickListener {
            // Tampilkan pesan sementara karena halaman RegisterActivity belum dibuat
            Toast.makeText(this, "Menuju halaman pendaftaran...", Toast.LENGTH_SHORT).show()

            /* Nanti kodenya diganti menjadi ini saat halamannya sudah ada:
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            */
        }
    }

    private fun prosesLogin(email: String, pass: String) {
        // Siapkan data JSON yang akan dikirim
        val requestData = HashMap<String, String>()
        requestData["email"] = email
        requestData["password"] = pass

        // Panggil API lewat Retrofit
        ApiClient.instance.loginPendamping(requestData).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    // Jika login benar, munculkan pesan selamat datang
                    val nama = response.body()?.data?.nama_pendamping
                    Toast.makeText(this@LoginActivity, "Selamat datang, $nama", Toast.LENGTH_SHORT).show()

                    // Pindah ke halaman Dashboard
                    val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
                    startActivity(intent)
                    finish() // Tutup halaman login agar tidak bisa di-back
                } else {
                    // Jika salah sandi/email
                    Toast.makeText(this@LoginActivity, "Login gagal: Email atau password salah", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                // Jika koneksi internet atau server mati
                Toast.makeText(this@LoginActivity, "Koneksi Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}