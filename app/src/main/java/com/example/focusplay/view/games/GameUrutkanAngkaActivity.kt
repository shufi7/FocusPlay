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
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class GameUrutkanAngkaActivity : AppCompatActivity() {

    private lateinit var gridLayoutAngka: GridLayout
    private lateinit var tvTargetAngka: TextView

    private val listAngkaAcak = (1..9).toList().shuffled()
    private var angkaYangHarusDitekan = 1
    private var idAnak = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_urutkan_angka)

        idAnak = intent.getStringExtra("ID_ANAK") ?: ""

        tvTargetAngka = findViewById(R.id.tvTargetAngka)
        gridLayoutAngka = findViewById(R.id.gridLayoutAngka)

        buatPapanAngkaOtomatis()
    }

    private fun buatPapanAngkaOtomatis() {
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val totalRuangKosong = (80 * displayMetrics.density).toInt()
        val ukuranKotak = (screenWidth - totalRuangKosong) / 3
        val marginKotak = (6 * displayMetrics.density).toInt()

        for (angka in listAngkaAcak) {
            val kotak = TextView(this)
            val params = GridLayout.LayoutParams()
            params.width = ukuranKotak
            params.height = ukuranKotak
            params.setMargins(marginKotak, marginKotak, marginKotak, marginKotak)
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

                Handler(Looper.getMainLooper()).postDelayed({
                    simpanSkorKeFirebase("Urutkan Angka", 100)
                }, 1500)
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

    private fun simpanSkorKeFirebase(namaGame: String, skorAkhir: Int) {
        if (idAnak.isEmpty()) { finish(); return }
        val db = FirebaseFirestore.getInstance()
        val dataSkor = hashMapOf("id_anak" to idAnak, "nama_game" to namaGame, "skor" to skorAkhir, "tanggal_main" to Date())
        db.collection("tb_riwayat_game").add(dataSkor).addOnSuccessListener { finish() }.addOnFailureListener { finish() }
    }
}