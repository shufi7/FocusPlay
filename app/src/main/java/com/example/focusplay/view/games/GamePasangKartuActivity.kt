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

class GamePasangKartuActivity : AppCompatActivity() {

    private lateinit var gridLayoutKartu: GridLayout
    private lateinit var tvStatus: TextView

    private val listEmojiHewan = listOf("🐶", "🐱", "🐰", "🦊", "🐻", "🐼", "🐶", "🐱", "🐰", "🦊", "🐻", "🐼").shuffled()
    private var kartuPertama: TextView? = null
    private var indexPertama: Int? = null
    private var isMengecek = false
    private var pasanganDitemukan = 0
    private var idAnak = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_pasang_kartu)

        idAnak = intent.getStringExtra("ID_ANAK") ?: ""

        tvStatus = findViewById(R.id.tvStatus)
        gridLayoutKartu = findViewById(R.id.gridLayoutKartu)

        buatPapanKartuOtomatis()
    }

    private fun buatPapanKartuOtomatis() {
        val ukuranKartu = (90 * resources.displayMetrics.density).toInt()
        val marginKartu = (8 * resources.displayMetrics.density).toInt()

        for (i in listEmojiHewan.indices) {
            val kartu = TextView(this)
            val params = GridLayout.LayoutParams()
            params.width = ukuranKartu
            params.height = ukuranKartu
            params.setMargins(marginKartu, marginKartu, marginKartu, marginKartu)
            kartu.layoutParams = params

            tutupKartu(kartu)

            kartu.setOnClickListener {
                if (isMengecek || kartu.text != "❓") return@setOnClickListener

                bukaKartu(kartu, listEmojiHewan[i])

                if (kartuPertama == null) {
                    kartuPertama = kartu
                    indexPertama = i
                } else {
                    isMengecek = true
                    cekKecocokanKartu(kartu, i)
                }
            }
            gridLayoutKartu.addView(kartu)
        }
    }

    private fun tutupKartu(kartu: TextView) {
        kartu.text = "❓"
        kartu.textSize = 36f
        kartu.gravity = Gravity.CENTER
        kartu.setBackgroundColor(Color.parseColor("#1565C0"))
        kartu.setTextColor(Color.WHITE)
        kartu.elevation = 8f
    }

    private fun bukaKartu(kartu: TextView, emoji: String) {
        kartu.text = emoji
        kartu.setBackgroundColor(Color.WHITE)
        kartu.elevation = 2f
    }

    private fun cekKecocokanKartu(kartuKedua: TextView, indexKedua: Int) {
        if (listEmojiHewan[indexPertama!!] == listEmojiHewan[indexKedua]) {
            pasanganDitemukan++
            tvStatus.text = "Pasangan: $pasanganDitemukan / 6"
            resetMemoriPengecekan()

            if (pasanganDitemukan == 6) {
                tvStatus.text = "Luar Biasa! 🎉"
                tvStatus.setTextColor(Color.parseColor("#4CAF50"))

                Handler(Looper.getMainLooper()).postDelayed({
                    simpanSkorKeFirebase("Pasang Kartu", 100)
                }, 1500)
            }
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                tutupKartu(kartuPertama!!)
                tutupKartu(kartuKedua)
                resetMemoriPengecekan()
            }, 1000)
        }
    }

    private fun resetMemoriPengecekan() {
        kartuPertama = null
        indexPertama = null
        isMengecek = false
    }

    private fun simpanSkorKeFirebase(namaGame: String, skorAkhir: Int) {
        if (idAnak.isEmpty()) { finish(); return }
        val db = FirebaseFirestore.getInstance()
        val dataSkor = hashMapOf("id_anak" to idAnak, "nama_game" to namaGame, "skor" to skorAkhir, "tanggal_main" to Date())
        db.collection("tb_riwayat_game").add(dataSkor).addOnSuccessListener { finish() }.addOnFailureListener { finish() }
    }
}