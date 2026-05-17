package com.example.focusplay.view

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import com.example.focusplay.utils.SessionManager

class DashboardActivity : AppCompatActivity() {

    private lateinit var session: SessionManager

    private lateinit var tvWelcomeName: TextView
    private lateinit var tvWaktuBermain: TextView
    private lateinit var tvRataAkurasi: TextView

    private lateinit var btnTambahAnak: LinearLayout
    private lateinit var btnRiwayatPermainan: LinearLayout
    private lateinit var btnPengaturanPermainan: LinearLayout
    private lateinit var btnLogout: LinearLayout

    private lateinit var chartWeekly: WeeklyLineChartView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        session = SessionManager(this)

        hubungkanView()
        tampilkanNamaUser()
        tampilkanRingkasanSementara()
        tampilkanGrafikSesi()
        aturAksiTombol()
    }

    private fun hubungkanView() {
        tvWelcomeName = findViewById(R.id.tvWelcomeName)
        tvWaktuBermain = findViewById(R.id.tvWaktuBermain)
        tvRataAkurasi = findViewById(R.id.tvRataAkurasi)

        btnTambahAnak = findViewById(R.id.btnTambahAnak)
        btnRiwayatPermainan = findViewById(R.id.btnRiwayatPermainan)
        btnPengaturanPermainan = findViewById(R.id.btnPengaturanPermainan)
        btnLogout = findViewById(R.id.btnLogout)

        chartWeekly = findViewById(R.id.chartWeekly)
    }

    private fun tampilkanNamaUser() {
        val namaUser = session.getNamaUser()

        tvWelcomeName.text = if (namaUser.isNotEmpty()) {
            "Halo, $namaUser!"
        } else {
            "Halo, Orang Tua!"
        }
    }

    private fun tampilkanRingkasanSementara() {
        // Nanti bagian ini diganti dari data sesi bermain asli
        tvWaktuBermain.text = "0 menit"
        tvRataAkurasi.text = "0%"
    }

    private fun tampilkanGrafikSesi() {
        /*
            Ini contoh struktur data sementara.
            Nanti bagian ini diganti dengan data asli dari Firebase/API.

            Alur data asli:
            Anak selesai bermain
            -> data sesi tersimpan
            -> dashboard mengambil data sesi
            -> data dihitung rata-rata per hari
            -> chartWeekly.setData(dataGrafik)
        */

        val dataGrafikSementara = listOf(
            DataGrafikHarian("Sen", 80f),
            DataGrafikHarian("Sel", 75f),
            DataGrafikHarian("Rab", 90f),
            DataGrafikHarian("Kam", 85f),
            DataGrafikHarian("Jum", 92f),
            DataGrafikHarian("Sab", 88f)
        )

        chartWeekly.setData(dataGrafikSementara)
    }

    private fun aturAksiTombol() {
        btnTambahAnak.setOnClickListener {
            startActivity(Intent(this, TambahAnakActivity::class.java))
        }

        btnRiwayatPermainan.setOnClickListener {
            startActivity(Intent(this, RiwayatPermainanActivity::class.java))
        }

        btnPengaturanPermainan.setOnClickListener {
            startActivity(Intent(this, PengaturanPermainanActivity::class.java))
        }

        btnLogout.setOnClickListener {
            session.logout()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}