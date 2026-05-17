package com.example.focusplay.view.games

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R

class GameUrutkanAngkaActivity : AppCompatActivity() {

    private lateinit var gridLayoutAngka: GridLayout
    private lateinit var tvTargetAngka: TextView

    // Membuat daftar angka 1-9 dan mengacak posisinya
    private val listAngkaAcak = (1..9).toList().shuffled()

    // Angka pertama yang harus dicari anak adalah 1
    private var angkaYangHarusDitekan = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_urutkan_angka)

        tvTargetAngka = findViewById(R.id.tvTargetAngka)
        gridLayoutAngka = findViewById(R.id.gridLayoutAngka)

        buatPapanAngkaOtomatis()
    }

    private fun buatPapanAngkaOtomatis() {
        val ukuranKotak = (100 * resources.displayMetrics.density).toInt()
        val marginKotak = (8 * resources.displayMetrics.density).toInt()

        for (angka in listAngkaAcak) {
            val kotak = TextView(this)
            val params = GridLayout.LayoutParams()
            params.width = ukuranKotak
            params.height = ukuranKotak
            params.setMargins(marginKotak, marginKotak, marginKotak, marginKotak)
            kotak.layoutParams = params

            // Desain awal kotak angka
            resetDesainKotak(kotak, angka)

            // Logika saat kotak ditekan
            kotak.setOnClickListener {
                cekTebakanAngka(kotak, angka)
            }

            gridLayoutAngka.addView(kotak)
        }
    }

    private fun resetDesainKotak(kotak: TextView, angka: Int) {
        kotak.text = angka.toString()
        kotak.textSize = 40f
        kotak.gravity = Gravity.CENTER
        kotak.setBackgroundColor(Color.parseColor("#FFB300")) // Warna Kuning/Orange
        kotak.setTextColor(Color.WHITE)
        kotak.elevation = 8f
    }

    private fun cekTebakanAngka(kotak: TextView, angkaYangDitekan: Int) {
        if (angkaYangDitekan == angkaYangHarusDitekan) {
            // BENAR! Angka sesuai urutan
            kotak.setBackgroundColor(Color.parseColor("#4CAF50")) // Ubah jadi Hijau
            kotak.elevation = 2f
            kotak.setOnClickListener(null) // Kunci kotak agar tidak bisa diklik lagi

            angkaYangHarusDitekan++ // Lanjut ke angka berikutnya

            if (angkaYangHarusDitekan > 9) {
                // Menang! Semua angka sudah diurutkan
                tvTargetAngka.text = "Selesai! Pintar Sekali! 🎉"
                tvTargetAngka.setTextColor(Color.parseColor("#4CAF50"))
                Toast.makeText(this, "Berhasil mengurutkan semua angka!", Toast.LENGTH_LONG).show()
            } else {
                tvTargetAngka.text = "Cari Angka: $angkaYangHarusDitekan"
            }

        } else if (angkaYangDitekan > angkaYangHarusDitekan) {
            // SALAH! Anak menekan angka yang lebih besar dari urutan seharusnya
            kotak.setBackgroundColor(Color.parseColor("#E53935")) // Beri peringatan warna Merah

            // Kembalikan ke warna asli setelah 0.3 detik
            Handler(Looper.getMainLooper()).postDelayed({
                kotak.setBackgroundColor(Color.parseColor("#FFB300"))
            }, 300)
        }
    }
}