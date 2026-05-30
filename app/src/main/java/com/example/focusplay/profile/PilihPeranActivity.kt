package com.example.focusplay.profile

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import com.example.focusplay.dashboard.DashboardActivity
import com.example.focusplay.utils.SessionManager

class PilihPeranActivity : AppCompatActivity() {

    private lateinit var session: SessionManager

    private lateinit var tvNamaUser: TextView
    private lateinit var cardOrangTua: LinearLayout
    private lateinit var cardAnak: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pilih_peran)

        supportActionBar?.hide()

        session = SessionManager(this)

        initView()
        tampilkanNamaUser()
        setupKlikPeran()
    }

    private fun initView() {
        tvNamaUser = findViewById(R.id.tvNamaUser)
        cardOrangTua = findViewById(R.id.cardOrangTua)
        cardAnak = findViewById(R.id.cardAnak)
    }

    private fun tampilkanNamaUser() {
        val namaUser = session.getNamaUser()
        val namaPanggilan = ambilNamaPanggilan(namaUser)

        tvNamaUser.text = if (namaPanggilan.isBlank()) {
            "Halo!"
        } else {
            "Halo, $namaPanggilan!"
        }
    }

    private fun ambilNamaPanggilan(namaLengkap: String?): String {
        if (namaLengkap.isNullOrBlank()) return ""

        return namaLengkap
            .trim()
            .split(" ")
            .firstOrNull()
            ?: ""
    }

    private fun setupKlikPeran() {
        cardOrangTua.setOnClickListener {
            bukaHalaman(DashboardActivity::class.java)
        }

        cardAnak.setOnClickListener {
            bukaHalaman(PilihAnakActivity::class.java)
        }
    }

    private fun bukaHalaman(tujuan: Class<*>) {
        val intent = Intent(this, tujuan)
        startActivity(intent)
    }
}