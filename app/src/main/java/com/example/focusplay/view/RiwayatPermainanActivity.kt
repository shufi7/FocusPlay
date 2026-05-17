package com.example.focusplay.view

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R

class RiwayatPermainanActivity : AppCompatActivity() {

    private lateinit var ivBack: ImageView

    private lateinit var tvTotalSesi: TextView
    private lateinit var tvRataAkurasiRiwayat: TextView
    private lateinit var tvTotalDurasi: TextView
    private lateinit var tvRiwayatKosong: TextView
    private lateinit var containerRiwayatPermainan: LinearLayout

    data class RiwayatPermainan(
        val namaGame: String,
        val namaAnak: String,
        val akurasi: Int,
        val skor: Int,
        val durasiMenit: Int,
        val tanggal: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_riwayat_permainan)

        hubungkanView()
        aturTombol()
        tampilkanRiwayatKosong()
    }

    private fun hubungkanView() {
        ivBack = findViewById(R.id.ivBackRiwayat)

        tvTotalSesi = findViewById(R.id.tvTotalSesi)
        tvRataAkurasiRiwayat = findViewById(R.id.tvRataAkurasiRiwayat)
        tvTotalDurasi = findViewById(R.id.tvTotalDurasi)
        tvRiwayatKosong = findViewById(R.id.tvRiwayatKosong)
        containerRiwayatPermainan = findViewById(R.id.containerRiwayatPermainan)
    }

    private fun aturTombol() {
        ivBack.setOnClickListener {
            finish()
        }
    }

    private fun tampilkanRiwayatKosong() {
        /*
            Tidak ada data dummy di sini.
            Nanti bagian ini bisa diganti dengan pemanggilan data asli dari Firebase/Firestore.
        */
        val dataRiwayat = emptyList<RiwayatPermainan>()

        renderRiwayat(dataRiwayat)
    }

    private fun renderRiwayat(dataRiwayat: List<RiwayatPermainan>) {
        containerRiwayatPermainan.removeAllViews()

        if (dataRiwayat.isEmpty()) {
            tvRiwayatKosong.visibility = TextView.VISIBLE
            tvTotalSesi.text = "0"
            tvRataAkurasiRiwayat.text = "0%"
            tvTotalDurasi.text = "0m"
            return
        }

        tvRiwayatKosong.visibility = TextView.GONE

        val totalSesi = dataRiwayat.size
        val rataAkurasi = dataRiwayat.map { it.akurasi }.average().toInt()
        val totalDurasi = dataRiwayat.sumOf { it.durasiMenit }

        tvTotalSesi.text = totalSesi.toString()
        tvRataAkurasiRiwayat.text = "$rataAkurasi%"
        tvTotalDurasi.text = "${totalDurasi}m"

        dataRiwayat.forEachIndexed { index, riwayat ->
            tambahCardRiwayat(riwayat, index)
        }
    }

    private fun tambahCardRiwayat(riwayat: RiwayatPermainan, index: Int) {
        val warnaIcon = when (index % 4) {
            0 -> "#F0FBEA"
            1 -> "#F4EEFF"
            2 -> "#EAF7FF"
            else -> "#FFF8E8"
        }

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))
            background = roundedDrawable("#FFFFFF", 22, "#E5EEF7")
            elevation = dp(2).toFloat()

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(96)
            ).apply {
                setMargins(0, 0, 0, dp(12))
            }
        }

        val icon = TextView(this).apply {
            text = "🎮"
            textSize = 22f
            gravity = Gravity.CENTER
            background = roundedDrawable(warnaIcon, 16, "#E5EEF7")

            layoutParams = LinearLayout.LayoutParams(dp(52), dp(52))
        }

        val textBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(dp(14), 0, dp(10), 0)
            }
        }

        val tvGame = TextView(this).apply {
            text = riwayat.namaGame
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#1F2937"))
        }

        val tvDetail = TextView(this).apply {
            text = "${riwayat.namaAnak} • Akurasi ${riwayat.akurasi}% • Skor ${riwayat.skor} • ${riwayat.durasiMenit} menit"
            textSize = 12.5f
            setTextColor(Color.parseColor("#6B7280"))
            setPadding(0, dp(4), 0, 0)
        }

        textBox.addView(tvGame)
        textBox.addView(tvDetail)

        val tvTanggal = TextView(this).apply {
            text = riwayat.tanggal
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#406915"))
            gravity = Gravity.CENTER
        }

        card.addView(icon)
        card.addView(textBox)
        card.addView(tvTanggal)

        containerRiwayatPermainan.addView(card)
    }

    private fun roundedDrawable(color: String, radius: Int, strokeColor: String): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(Color.parseColor(color))
            cornerRadius = dp(radius).toFloat()
            setStroke(dp(1), Color.parseColor(strokeColor))
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}