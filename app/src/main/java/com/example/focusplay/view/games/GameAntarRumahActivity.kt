package com.example.focusplay.view.games

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R

class GameAntarRumahActivity : AppCompatActivity() {

    private lateinit var viewMobil: TextView
    private lateinit var viewRumah: TextView
    private lateinit var tvInstruksi: TextView
    private lateinit var layStatus: View

    // Variabel untuk menghitung posisi pergerakan jari
    private var dX = 0f
    private var dY = 0f

    // Variabel untuk mengembalikan mobil jika gagal masuk
    private var posisiAwalX = 0f
    private var posisiAwalY = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_antar_rumah)

        viewMobil = findViewById(R.id.viewMobil)
        viewRumah = findViewById(R.id.viewRumah)
        tvInstruksi = findViewById(R.id.tvInstruksi)
        layStatus = findViewById(R.id.layStatus)

        // Simpan titik mula-mula mobil setelah layar selesai dimuat
        viewMobil.post {
            posisiAwalX = viewMobil.x
            posisiAwalY = viewMobil.y
        }

        // Mesin penangkap gerakan Drag and Drop
        viewMobil.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Saat jari pertama kali menyentuh mobil
                    dX = view.x - event.rawX
                    dY = view.y - event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    // Saat jari menggeser mobil di layar
                    view.animate()
                        .x(event.rawX + dX)
                        .y(event.rawY + dY)
                        .setDuration(0) // Tidak ada penundaan animasi agar terasa instan
                        .start()
                }
                MotionEvent.ACTION_UP -> {
                    // Saat jari dilepas dari layar, lakukan pengecekan
                    cekKeberhasilan()
                }
                else -> return@setOnTouchListener false
            }
            true // Mengembalikan true berarti kita mengambil alih penuh sentuhan ini
        }
    }

    private fun cekKeberhasilan() {
        // Cari titik tengah dari si mobil
        val mobilTengahX = viewMobil.x + (viewMobil.width / 2)
        val mobilTengahY = viewMobil.y + (viewMobil.height / 2)

        // Dapatkan batas-batas tepi dari rumah
        val rumahKiri = viewRumah.x
        val rumahKanan = viewRumah.x + viewRumah.width
        val rumahAtas = viewRumah.y
        val rumahBawah = viewRumah.y + viewRumah.height

        // Cek! Apakah titik tengah mobil berada tepat di dalam kotak rumah?
        if (mobilTengahX >= rumahKiri && mobilTengahX <= rumahKanan &&
            mobilTengahY >= rumahAtas && mobilTengahY <= rumahBawah) {

            // JIKA MASUK (BERHASIL)
            tvInstruksi.text = "Berhasil! 🚗 sudah di rumah! 🎉"
            layStatus.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50")) // Ubah banner jadi Hijau
            tvInstruksi.setTextColor(android.graphics.Color.parseColor("#FFFFFF"))

            // Kunci mobilnya agar tidak bisa ditarik keluar lagi
            viewMobil.setOnTouchListener(null)

            Toast.makeText(this, "Koordinasi tangan dan matamu sangat bagus!", Toast.LENGTH_LONG).show()
        } else {
            // JIKA MELESET (GAGAL)
            // Lontarkan mobil kembali ke posisi awalnya perlahan-lahan
            viewMobil.animate()
                .x(posisiAwalX)
                .y(posisiAwalY)
                .setDuration(300) // Waktu kembali: 0.3 detik
                .start()
        }
    }
}