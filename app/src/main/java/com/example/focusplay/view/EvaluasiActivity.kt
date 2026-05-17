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
import java.util.Date

// Struktur data lokal untuk mempermudah penyusunan statistik
data class RiwayatGame(val namaGame: String, val skor: Long, val tanggal: Date)

class EvaluasiActivity : AppCompatActivity() {

    private lateinit var tvNamaAnakEvaluasi: TextView
    private lateinit var tvStatistikVisual: TextView
    private lateinit var tvHasilAI: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnTutupEvaluasi: Button

    private lateinit var db: FirebaseFirestore
    private var idAnak = ""
    private var namaAnak = ""

    // PASTIKAN API KEY KAMU TERTULIS DI SINI:
    private val GEMINI_API_KEY = "AIzaSyAAfdyLY4NCgosYBj1FwmkyDqYsXyIEV0A"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evaluasi)

        idAnak = intent.getStringExtra("ID_ANAK") ?: ""
        namaAnak = intent.getStringExtra("NAMA_ANAK") ?: "Anak"

        tvNamaAnakEvaluasi = findViewById(R.id.tvNamaAnakEvaluasi)
        tvStatistikVisual = findViewById(R.id.tvStatistikVisual)
        tvHasilAI = findViewById(R.id.tvHasilAI)
        progressBar = findViewById(R.id.progressBar)
        btnTutupEvaluasi = findViewById(R.id.btnTutupEvaluasi)

        db = FirebaseFirestore.getInstance()

        tvNamaAnakEvaluasi.text = "🧠 Analisis AI untuk $namaAnak"

        btnTutupEvaluasi.setOnClickListener { finish() }

        ambilRiwayatDanSusunStatistik()
    }

    private fun ambilRiwayatDanSusunStatistik() {
        progressBar.visibility = View.VISIBLE
        tvHasilAI.text = "Menganalisis pola permainan..."
        tvStatistikVisual.text = "Memuat data dari cloud..."

        db.collection("tb_riwayat_game")
            .whereEqualTo("id_anak", idAnak)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    progressBar.visibility = View.GONE
                    tvStatistikVisual.text = "Belum ada data permainan."
                    tvHasilAI.text = "Silakan minta anak memainkan beberapa game terlebih dahulu agar AI bisa memberikan evaluasi."
                    return@addOnSuccessListener
                }

                // 1. Tarik semua data mentah ke dalam list lokal
                val listRiwayat = mutableListOf<RiwayatGame>()
                for (doc in documents) {
                    val namaGame = doc.getString("nama_game") ?: "Tidak Diketahui"
                    val skor = doc.getLong("skor") ?: 0
                    val tanggal = doc.getDate("tanggal_main") ?: Date()
                    listRiwayat.add(RiwayatGame(namaGame, skor, tanggal))
                }

                // 2. Urutkan berdasarkan tanggal (paling lama ke terbaru)
                listRiwayat.sortBy { it.tanggal }

                // 3. Kelompokkan berdasarkan jenis game
                val dikelompokkanPerGame = listRiwayat.groupBy { it.namaGame }

                val teksUIStatistik = StringBuilder()
                val teksPromptAI = StringBuilder()

                teksPromptAI.append("Berikut adalah riwayat skor permainan anak bernama $namaAnak. Skor ditulis secara berurutan dari waktu terlama hingga terbaru (menunjukkan progres):\n\n")

                // 4. Susun format visual (10 ➔ 15 ➔ 20)
                for ((namaGame, riwayat) in dikelompokkanPerGame) {
                    val daftarSkor = riwayat.map { it.skor }
                    val progresSkorStr = daftarSkor.joinToString(" ➔ ")

                    // Cetak untuk layar Orang Tua
                    teksUIStatistik.append("🎮 $namaGame\nSkor: $progresSkorStr\n\n")

                    // Cetak untuk dibaca mesin AI
                    teksPromptAI.append("- $namaGame: $progresSkorStr\n")
                }

                tvStatistikVisual.text = teksUIStatistik.toString().trimEnd()

                teksPromptAI.append("\nSebagai psikolog anak dan ahli stimulasi kognitif, tolong analisis tren skor di atas. Apakah ada peningkatan, stagnan, atau fluktuatif? Hubungkan setiap game dengan fungsi kognitif yang dilatih (misal: refleks, koordinasi visual, memori, logika). Berikan kesimpulan dan saran edukatif yang sangat singkat, padat, dan ramah untuk orang tua.")

                // 5. Lempar data matang ini ke AI
                panggilGeminiAI(teksPromptAI.toString())
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                tvStatistikVisual.text = "Gagal memuat data."
                tvHasilAI.text = "Terjadi kesalahan sistem."
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun panggilGeminiAI(prompt: String) {
        val generativeModel = GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = GEMINI_API_KEY
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = generativeModel.generateContent(prompt)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    tvHasilAI.text = response.text ?: "AI tidak memberikan respon."
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    tvHasilAI.text = "Gagal memproses analisis AI. Pastikan internet aktif."
                    Toast.makeText(this@EvaluasiActivity, "AI Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}