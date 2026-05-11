package com.example.focusplay.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Memberikan jeda waktu 3000 milidetik (3 detik)
        Handler(Looper.getMainLooper()).postDelayed({
            // Berpindah dari MainActivity (Splash) ke LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)

            // Menutup MainActivity agar saat ditekan tombol "Back", tidak kembali ke Splash Screen
            finish()
        }, 3000)
    }
}