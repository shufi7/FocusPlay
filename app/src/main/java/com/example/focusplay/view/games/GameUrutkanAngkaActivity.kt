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

    private val listAngkaAcak = (1..9).toList().shuffled()
    private var angkaYangHarusDitekan = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_urutkan_angka)

        tvTargetAngka = findViewById(R.id.tvTargetAngka)
        gridLayoutAngka = findViewById(R.id.gridLayoutAngka)

        buatPapanAngkaOtomatis()
    }

    private fun buatPapanAngkaOtomatis() {
        // --- PERBAIKAN UX/UI: Kalkulasi ukuran kotak secara dinamis menyesuaikan lebar HP ---
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels

        // Kita hitung total ruang yang mau disisakan untuk batas layar (padding) dan jarak antar kotak
        val totalRuangKosong = (80 * displayMetrics.density).toInt()

        // Ukuran 1 kotak = (Lebar layar penuh - total ruang kosong) dibagi 3 kolom
        val ukuranKotak = (screenWidth - totalRuangKosong) / 3
        val marginKotak = (6 * displayMetrics.density).toInt() // Jarak tipis antar kotak

        for (angka in listAngkaAcak) {
            val kotak = TextView(this)
            val params = GridLayout.LayoutParams()

            // Terapkan hasil perhitungan matematika ke kotak
            params.width = ukuranKotak
            params.height = ukuranKotak
            params.setMargins(marginKotak, marginKotak, marginKotak, marginKotak)

            // Kunci posisi kotak agar benar-benar diam di tengah sel Grid-nya
            params.setGravity(Gravity.CENTER)
            kotak.layoutParams = params

            resetDesainKotak(kotak, angka)

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
        kotak.setBackgroundColor(Color.parseColor("#FFB300"))
        kotak.setTextColor(Color.WHITE)
        kotak.elevation = 8f
    }

    private fun cekTebakanAngka(kotak: TextView, angkaYangDitekan: Int) {
        if (angkaYangDitekan == angkaYangHarusDitekan) {
            kotak.setBackgroundColor(Color.parseColor("#4CAF50"))
            kotak.elevation = 2f
            kotak.setOnClickListener(null)

            angkaYangHarusDitekan++

            if (angkaYangHarusDitekan > 9) {
                tvTargetAngka.text = "Selesai! Pintar Sekali! 🎉"
                tvTargetAngka.setTextColor(Color.parseColor("#4CAF50"))
                Toast.makeText(this, "Berhasil mengurutkan semua angka!", Toast.LENGTH_LONG).show()
            } else {
                tvTargetAngka.text = "Cari Angka: $angkaYangHarusDitekan"
            }

        } else if (angkaYangDitekan > angkaYangHarusDitekan) {
            kotak.setBackgroundColor(Color.parseColor("#E53935"))

            Handler(Looper.getMainLooper()).postDelayed({
                kotak.setBackgroundColor(Color.parseColor("#FFB300"))
            }, 300)
        }
    }
}