package com.example.focusplay.view

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import com.example.focusplay.utils.SessionManager

class PilihPeranActivity : AppCompatActivity() {

    private lateinit var session: SessionManager

    private lateinit var tvNamaUser: TextView
    private lateinit var cardOrangTua: LinearLayout
    private lateinit var cardAnak: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pilih_peran)

        session = SessionManager(this)

        tvNamaUser = findViewById(R.id.tvNamaUser)
        cardOrangTua = findViewById(R.id.cardOrangTua)
        cardAnak = findViewById(R.id.cardAnak)

        val namaUser = session.getNamaUser()
        tvNamaUser.text = "Halo, $namaUser!"

        // Mengarahkan tombol Orang Tua LANGSUNG ke Dasbor (Tanpa PIN)
        cardOrangTua.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
        }

        // Mengarahkan tombol Anak (Sementara masih ke TambahAnak)
        cardAnak.setOnClickListener {
            val intent = Intent(this, TambahAnakActivity::class.java)
            startActivity(intent)
        }
    }
}