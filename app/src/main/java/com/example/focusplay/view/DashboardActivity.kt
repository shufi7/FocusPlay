package com.example.focusplay.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import com.example.focusplay.utils.SessionManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class DashboardActivity : AppCompatActivity() {

    private lateinit var tvWelcomeName: TextView
    private lateinit var btnLogout: Button
    private lateinit var btnKeTambahAnak: Button
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        tvWelcomeName = findViewById(R.id.tvWelcomeName)
        btnLogout = findViewById(R.id.btnLogout)
        btnKeTambahAnak = findViewById(R.id.btnKeTambahAnak)

        session = SessionManager(this)

        // Ambil nama dari sesi (yang sekarang didapat dari akun Google)
        val namaUser = session.getNamaUser()
        tvWelcomeName.text = "Halo, $namaUser!"

        btnKeTambahAnak.setOnClickListener {
            startActivity(Intent(this, TambahAnakActivity::class.java))
        }

        // --- LOGIKA LOGOUT FIREBASE ---
        btnLogout.setOnClickListener {
            // 1. Hapus sesi lokal
            session.logout()

            // 2. Keluar dari Firebase
            FirebaseAuth.getInstance().signOut()

            // 3. Keluar dari Google Sign-In agar muncul pilihan akun lagi saat login berikutnya
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            GoogleSignIn.getClient(this, gso).signOut().addOnCompleteListener {

                // Lempar kembali ke halaman Login
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }
}