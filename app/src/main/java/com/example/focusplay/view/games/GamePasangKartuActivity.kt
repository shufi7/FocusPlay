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

class GamePasangKartuActivity : AppCompatActivity() {

    private lateinit var gridLayoutKartu: GridLayout
    private lateinit var tvStatus: TextView

    // Daftar 6 pasang Emoji yang sudah diacak posisinya
    private val listEmojiHewan = listOf(
        "🐶", "🐱", "🐰", "🦊", "🐻", "🐼",
        "🐶", "🐱", "🐰", "🦊", "🐻", "🐼"
    ).shuffled()

    // Variabel Memori (Menyimpan status kartu yang sedang ditekan)
    private var kartuPertama: TextView? = null
    private var indexPertama: Int? = null
    private var isMengecek = false // Agar anak tidak bisa spam klik saat kartu sedang dicek
    private var pasanganDitemukan = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_pasang_kartu)

        tvStatus = findViewById(R.id.tvStatus)
        gridLayoutKartu = findViewById(R.id.gridLayoutKartu)

        buatPapanKartuOtomatis()
    }

    private fun buatPapanKartuOtomatis() {
        // Atur ukuran kartu agar pas di layar (sekitar 90dp)
        val ukuranKartu = (90 * resources.displayMetrics.density).toInt()
        val marginKartu = (8 * resources.displayMetrics.density).toInt()

        // Looping untuk mencetak 12 kartu
        for (i in listEmojiHewan.indices) {
            val kartu = TextView(this)
            val params = GridLayout.LayoutParams()
            params.width = ukuranKartu
            params.height = ukuranKartu
            params.setMargins(marginKartu, marginKartu, marginKartu, marginKartu)
            kartu.layoutParams = params

            // Desain awal kartu (Tertutup)
            tutupKartu(kartu)

            // Logika saat kartu ditekan
            kartu.setOnClickListener {
                // Jika sedang mengecek atau kartu sudah terbuka, abaikan klik
                if (isMengecek || kartu.text != "❓") return@setOnClickListener

                // Buka Kartu
                bukaKartu(kartu, listEmojiHewan[i])

                if (kartuPertama == null) {
                    // Ini adalah tebakan pertama
                    kartuPertama = kartu
                    indexPertama = i
                } else {
                    // Ini adalah tebakan kedua
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
        kartu.setBackgroundColor(Color.parseColor("#1565C0")) // Warna Biru Gelap
        kartu.setTextColor(Color.WHITE)
        kartu.elevation = 8f
    }

    private fun bukaKartu(kartu: TextView, emoji: String) {
        kartu.text = emoji
        kartu.setBackgroundColor(Color.WHITE)
        kartu.elevation = 2f
    }

    private fun cekKecocokanKartu(kartuKedua: TextView, indexKedua: Int) {
        // Bandingkan emoji di index pertama dan kedua
        if (listEmojiHewan[indexPertama!!] == listEmojiHewan[indexKedua]) {
            // BENAR! Pasangan cocok
            pasanganDitemukan++
            tvStatus.text = "Pasangan: $pasanganDitemukan / 6"

            resetMemoriPengecekan()

            // Jika semua sudah ditemukan
            if (pasanganDitemukan == 6) {
                tvStatus.text = "Luar Biasa! 🎉"
                tvStatus.setTextColor(Color.parseColor("#4CAF50"))
                Toast.makeText(this, "Semua hewan berhasil dipasangkan!", Toast.LENGTH_LONG).show()
            }
        } else {
            // SALAH! Tutup kembali kedua kartu setelah menunggu 1 detik
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
}