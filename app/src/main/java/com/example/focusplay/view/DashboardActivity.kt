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
        tampilkanRingkasanAwal()
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

    private fun tampilkanRingkasanAwal() {
        /*
            Karena belum tersambung ke data sesi bermain,
            nilai ringkasan dibuat 0 dulu.
        */
        tvWaktuBermain.text = "0 menit"
        tvRataAkurasi.text = "0%"
    }

    private fun tampilkanGrafikSesi() {
        /*
            Jangan isi data dummy di sini.
            Kalau belum ada anak yang bermain, grafik harus kosong.

            Nanti kalau Firebase/data sesi sudah siap,
            emptyList() ini diganti dengan data asli dari sesi bermain anak.
        */
        chartWeekly.setData(emptyList())
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