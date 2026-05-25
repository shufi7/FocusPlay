package com.example.focusplay.history

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R

class EvaluasiActivity : AppCompatActivity() {

    private lateinit var ivBackEvaluasi: ImageView
    private lateinit var tvNamaAnakEvaluasi: TextView
    private lateinit var layLoadingAI: LinearLayout
    private lateinit var tvHasilAI: TextView
    private lateinit var btnRefreshEvaluasi: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evaluasi)

        // Hubungkan dengan XML (Error garis merah akan hilang setelah XML di atas di-paste)
        ivBackEvaluasi = findViewById(R.id.ivBackEvaluasi)
        tvNamaAnakEvaluasi = findViewById(R.id.tvNamaAnakEvaluasi)
        layLoadingAI = findViewById(R.id.layLoadingAI)
        tvHasilAI = findViewById(R.id.tvHasilAI)
        btnRefreshEvaluasi = findViewById(R.id.btnRefreshEvaluasi)

        // Set nama anak di header
        val namaAnak = intent.getStringExtra("NAMA_ANAK") ?: "Anak"
        tvNamaAnakEvaluasi.text = "Analisis perkembangan $namaAnak"

        // Tombol kembali
        ivBackEvaluasi.setOnClickListener { finish() }
        btnRefreshEvaluasi.setOnClickListener { finish() }

        // MENGAMBIL HASIL AI DARI GAME
        val evaluasiLangsung = intent.getStringExtra("EVALUASI_LANGSUNG")

        if (evaluasiLangsung != null) {
            // Anak baru saja selesai main, langsung tampilkan hasilnya!
            layLoadingAI.visibility = View.GONE
            tvHasilAI.visibility = View.VISIBLE
            tvHasilAI.text = evaluasiLangsung

            btnRefreshEvaluasi.visibility = View.VISIBLE
            btnRefreshEvaluasi.text = "Kembali ke Menu"
        } else {
            // Jika masuk dari Dashboard (bukan dari selesai main)
            layLoadingAI.visibility = View.VISIBLE
            tvHasilAI.visibility = View.GONE
        }
    }
}