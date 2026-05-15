package com.example.focusplay.view

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import com.example.focusplay.model.LoginResponse
import com.example.focusplay.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    // Deklarasi variabel
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var ivTogglePassword: ImageView // Variabel baru untuk ikon mata

    private var isPasswordVisible = false // Status untuk melacak sandi terlihat/sembunyi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Hubungkan variabel dengan ID di XML
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)
        ivTogglePassword = findViewById(R.id.ivTogglePassword) // Hubungkan ikon mata

        // --- LOGIKA IKON MATA SANDI ---
        ivTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible // Balikkan status
            if (isPasswordVisible) {
                // Tampilkan teks sandi
                etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                ivTogglePassword.setColorFilter(Color.parseColor("#406915")) // Ikon jadi hijau saat dilihat
            } else {
                // Sembunyikan kembali jadi titik-titik
                etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                ivTogglePassword.setColorFilter(Color.parseColor("#555555")) // Ikon kembali abu-abu
            }
            // Pindahkan kursor pengetikan ke ujung karakter terakhir
            etPassword.setSelection(etPassword.text.length)
        }

        // --- LOGIKA TOMBOL MASUK ---
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            prosesLogin(email, password)
        }

        // --- LOGIKA TOMBOL DAFTAR ---
        btnRegister.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun prosesLogin(email: String, pass: String) {
        val requestData = HashMap<String, String>()
        requestData["email"] = email
        requestData["password"] = pass

        ApiClient.instance.loginPendamping(requestData).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    val nama = response.body()?.data?.nama_pendamping
                    Toast.makeText(this@LoginActivity, "Selamat datang, $nama", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Login gagal: Email atau password salah", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Koneksi Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}