package com.example.focusplay.view

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.Date
import java.util.concurrent.TimeUnit

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

    // KREDENSIAL FREEMODEL AI KAMU
    private val FREEMODEL_API_KEY = "fe_oa_1b4e26927d2353545e6de41533c50dc6446a02d577f04ed6"
    private val URL_API = "https://api.freemodel.dev/v1/chat/completions"

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

        tvNamaAnakEvaluasi.text = "🧠 Analisis FreeModel AI untuk $namaAnak"

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
                    tvHasilAI.text = "Silakan minta anak memainkan beberapa game terlebih dahulu."
                    return@addOnSuccessListener
                }

                val listRiwayat = mutableListOf<RiwayatGame>()
                for (doc in documents) {
                    val namaGame = doc.getString("nama_game") ?: "Tidak Diketahui"
                    val skor = doc.getLong("skor") ?: 0
                    val tanggal = doc.getDate("tanggal_main") ?: Date()
                    listRiwayat.add(RiwayatGame(namaGame, skor, tanggal))
                }

                listRiwayat.sortBy { it.tanggal }
                val dikelompokkanPerGame = listRiwayat.groupBy { it.namaGame }

                val teksUIStatistik = StringBuilder()
                val teksPromptAI = StringBuilder()

                teksPromptAI.append("Berikut adalah riwayat skor permainan anak bernama $namaAnak. Skor diurutkan dari waktu terlama hingga terbaru:\n\n")

                for ((namaGame, riwayat) in dikelompokkanPerGame) {
                    val daftarSkor = riwayat.map { it.skor }
                    val progresSkorStr = daftarSkor.joinToString(" ➔ ")

                    teksUIStatistik.append("🎮 $namaGame\nSkor: $progresSkorStr\n\n")
                    teksPromptAI.append("- $namaGame: $progresSkorStr\n")
                }

                tvStatistikVisual.text = teksUIStatistik.toString().trimEnd()

                teksPromptAI.append("\nSebagai psikolog anak, analisis tren skor di atas. Apakah ada peningkatan? Hubungkan dengan fungsi kognitif yang dilatih. Berikan saran untuk orang tua.")

                // Panggil server FreeModel AI
                panggilFreeModelAI(teksPromptAI.toString())
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                tvStatistikVisual.text = "Gagal memuat data."
                tvHasilAI.text = "Terjadi kesalahan sistem."
            }
    }

    private fun panggilFreeModelAI(promptUser: String) {
        // Siapkan klien HTTP (Browser tanpa layar)
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

        // Susun payload JSON sesuai standar antarmuka chat
        val jsonPayload = JSONObject()
        jsonPayload.put("model", "gpt-5.5") // Sesuai config kamu

        val pesanArray = JSONArray()
        val pesanSystem = JSONObject()
        pesanSystem.put("role", "system")
        pesanSystem.put("content", "Kamu adalah psikolog anak yang ahli.")
        val pesanUser = JSONObject()
        pesanUser.put("role", "user")
        pesanUser.put("content", promptUser)

        pesanArray.put(pesanSystem)
        pesanArray.put(pesanUser)
        jsonPayload.put("messages", pesanArray)

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jsonPayload.toString().toRequestBody(mediaType)

        // Buat paket pengiriman
        val request = Request.Builder()
            .url(URL_API)
            .post(body)
            .addHeader("Authorization", "Bearer $FREEMODEL_API_KEY") // API Key dimasukkan di sini
            .addHeader("Content-Type", "application/json")
            .build()

        // Kirim permintaan di latar belakang
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    tvHasilAI.text = "Gagal menghubungi server FreeModel: ${e.message}"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()

                runOnUiThread {
                    progressBar.visibility = View.GONE
                    if (response.isSuccessful && responseData != null) {
                        try {
                            // Ekstrak teks balasan dari JSON
                            val jsonResponse = JSONObject(responseData)
                            val balasanAI = jsonResponse
                                .getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content")

                            tvHasilAI.text = balasanAI

                        } catch (e: Exception) {
                            tvHasilAI.text = "Gagal membaca format jawaban dari FreeModel."
                        }
                    } else {
                        tvHasilAI.text = "Ditolak oleh Server. Kode Error: ${response.code}\nPastikan API Key dan nama model valid."
                    }
                }
            }
        })
    }
}