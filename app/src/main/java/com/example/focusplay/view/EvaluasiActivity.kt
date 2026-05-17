package com.example.focusplay.view

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EvaluasiActivity : AppCompatActivity() {

    private lateinit var tvNamaAnakEvaluasi: TextView
    private lateinit var tvHasilAI: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnTutupEvaluasi: Button

    private lateinit var db: FirebaseFirestore
    private var idAnak = ""
    private var namaAnak = ""

    // Taruh API Key Gemini AI kamu di sini
    private val GEMINI_API_KEY = "AIzaSyAAfdyLY4NCgosYBj1FwmkyDqYsXyIEV0A"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evaluasi)

        idAnak = intent.getStringExtra("ID_ANAK") ?: ""
        namaAnak = intent.getStringExtra("NAMA_ANAK") ?: "Anak"

        tvNamaAnakEvaluasi = findViewById(R.id.tvNamaAnakEvaluasi)
        tvHasilAI = findViewById(R.id.tvHasilAI)
        progressBar = findViewById(R.id.progressBar)
        btnTutupEvaluasi = findViewById(R.id.btnTutupEvaluasi)

        db = FirebaseFirestore.getInstance()

        tvNamaAnakEvaluasi.text = "Laporan Analisis: $namaAnak"

        btnTutupEvaluasi.setOnClickListener { finish() }

        ambilRiwayatDanTanyaAI()
    }

    private fun ambilRiwayatDanTanyaAI() {
        progressBar.visibility = View.VISIBLE
        tvHasilAI.text = "Sedang menganalisis skor permainan..."

        // Tarik data riwayat bermain dari Firestore berdasarkan ID Anak
        db.collection("tb_riwayat_game")
            .whereEqualTo("id_anak", idAnak)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    progressBar.visibility = View.GONE
                    tvHasilAI.text = "Belum ada riwayat permainan untuk melakukan evaluasi. Silakan mainkan beberapa game terlebih dahulu di mode anak!"
                    return@addOnSuccessListener
                }

                // Susun data riwayat menjadi baris teks untuk dibaca AI
                val stringBuilder = StringBuilder()
                stringBuilder.append("Berikut adalah data skor permainan anak bernama $namaAnak:\n")

                for (doc in documents) {
                    val namaGame = doc.getString("nama_game") ?: "Game"
                    val skor = doc.getLong("skor") ?: 0
                    stringBuilder.append("- Game: $namaGame, Skor yang diperoleh: $skor\n")
                }

                stringBuilder.append("\nSebagai psikolog anak dan ahli stimulasi kognitif, berikan evaluasi singkat, saran edukatif yang ramah, dan aspek psikologis apa yang berkembang (misal atensi, refleks, memori). Gunakan bahasa Indonesia yang santun dan mudah dipahami orang tua.")

                // Kirim teks prompt tersebut ke Gemini AI
                panggilGeminiAI(stringBuilder.toString())
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                tvHasilAI.text = "Gagal memuat data dari awan."
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun panggilGeminiAI(prompt: String) {
        // Inisialisasi model komputasi generasi 2.5 yang cepat, aktif, dan gratis
        val generativeModel = GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = GEMINI_API_KEY
        )

        // Jalankan proses di latar belakang (Coroutines) agar aplikasi tidak hang
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = generativeModel.generateContent(prompt)

                // Kembalikan hasilnya ke antarmuka layar utama
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    tvHasilAI.text = response.text ?: "AI tidak memberikan respon."
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    tvHasilAI.text = "Gagal terhubung ke AI. Pastikan API Key benar dan internet aktif."
                    Toast.makeText(this@EvaluasiActivity, "AI Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}