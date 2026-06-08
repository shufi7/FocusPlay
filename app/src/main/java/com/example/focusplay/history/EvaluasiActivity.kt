package com.example.focusplay.history

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.focusplay.BuildConfig
import com.example.focusplay.R
import com.example.focusplay.dashboard.DashboardAnakActivity
import com.example.focusplay.games.GameDescriptionActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EvaluasiActivity : AppCompatActivity() {

    private var idAnak = ""
    private var namaAnak = "Anak"
    private var usiaAnak = 0
    private var namaGame = "Antar Si Domba"
    private var gameKey = "antar_rumah"

    companion object {
        private const val MODEL_AI = "gpt-5.5"
        private const val FREEMODEL_CHAT_URL = "https://api.freemodel.dev/v1/chat/completions"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evaluasi)

        ambilData()
        tampilkanHasil()
        aturTombol()
    }

    private fun ambilData() {
        idAnak = intent.getStringExtra("ID_ANAK") ?: ""
        namaAnak = intent.getStringExtra("NAMA_ANAK") ?: "Anak"
        usiaAnak = intent.getIntExtra("USIA_ANAK", 0)
        namaGame = intent.getStringExtra("NAMA_GAME") ?: "Antar Si Domba"
        gameKey = intent.getStringExtra("GAME_KEY") ?: "antar_rumah"
    }

    private fun tampilkanHasil() {
        val skor = intent.getIntExtra("SKOR", 0)
        val akurasi = intent.getIntExtra("AKURASI", 0)
        val durasiDetik = intent.getIntExtra("DURASI_DETIK", 0)
        val fase = intent.getIntExtra("FASE_AKHIR", 1)
        val hasilAI = intent.getStringExtra("EVALUASI_LANGSUNG")

        findViewById<TextView>(R.id.tvJudulHasil).text =
            "${namaAnak.lowercase().replaceFirstChar { it.titlecase() }} selesai bermain."
        findViewById<TextView>(R.id.tvRingkasanHasil).text =
            "Ringkasan sesi $namaGame sudah tersimpan. Gunakan hasil ini untuk melihat skor, akurasi, fase akhir, dan catatan pendampingan."
        findViewById<TextView>(R.id.tvNamaAnakEvaluasi).text = namaAnak
        findViewById<TextView>(R.id.tvInisialAnak).text =
            namaAnak.trim().firstOrNull()?.uppercase() ?: "A"
        findViewById<TextView>(R.id.tvInfoGame).text = "Game       $namaGame"
        findViewById<TextView>(R.id.tvInfoTanggal).text =
            "Tanggal    ${SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())}"

        findViewById<TextView>(R.id.tvEvaluasiSkor).text = skor.toString()
        findViewById<TextView>(R.id.tvEvaluasiAkurasi).text = "$akurasi%"
        findViewById<TextView>(R.id.tvEvaluasiDurasi).text = "${durasiDetik}s"
        findViewById<TextView>(R.id.tvEvaluasiFase).text = fase.toString()

        val fokus = when {
            akurasi >= 80 -> "Baik"
            akurasi >= 60 -> "Cukup"
            else -> "Berlatih"
        }
        val rekomendasi = if (akurasi >= 80 && fase < 3) "Naik" else "Tetap"
        val rekomendasiDetail =
            if (rekomendasi == "Naik") "Coba tantangan berikutnya" else "Pertahankan ritme bermain"

        findViewById<TextView>(R.id.tvFokus).text = fokus
        findViewById<TextView>(R.id.tvRekomendasi).text = rekomendasi
        findViewById<TextView>(R.id.tvRekomendasiDetail).text = rekomendasiDetail

        val loading = findViewById<LinearLayout>(R.id.layLoadingAI)
        val catatan = findViewById<TextView>(R.id.tvHasilAI)
        if (perluGenerateEvaluasiAi(hasilAI)) {
            loading.visibility = View.VISIBLE
            catatan.visibility = View.GONE
            generateEvaluasiAi(
                skor = skor,
                akurasi = akurasi,
                durasiDetik = durasiDetik,
                fase = fase,
                loading = loading,
                catatan = catatan
            )
        } else {
            loading.visibility = View.GONE
            catatan.visibility = View.VISIBLE
            catatan.text = hasilAI
        }
    }

    private fun perluGenerateEvaluasiAi(hasilAI: String?): Boolean {
        if (hasilAI.isNullOrBlank()) return true

        return hasilAI.contains(
            "Saran untuk ortu: pertahankan durasi bermain",
            ignoreCase = true
        )
    }

    private fun generateEvaluasiAi(
        skor: Int,
        akurasi: Int,
        durasiDetik: Int,
        fase: Int,
        loading: LinearLayout,
        catatan: TextView
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val hasil = panggilModelEvaluasi(
                skor = skor,
                akurasi = akurasi,
                durasiDetik = durasiDetik,
                fase = fase
            )

            withContext(Dispatchers.Main) {
                loading.visibility = View.GONE
                catatan.visibility = View.VISIBLE
                catatan.text = hasil
            }
        }
    }

    private fun panggilModelEvaluasi(
        skor: Int,
        akurasi: Int,
        durasiDetik: Int,
        fase: Int
    ): String {
        val apiKey = BuildConfig.FREEMODEL_API_KEY.trim()

        if (apiKey.isBlank()) {
            Log.e("EVALUASI_AI", "FREEMODEL_API_KEY kosong. Periksa local.properties.")
            return buatEvaluasiCadangan(skor, akurasi, fase)
        }

        return try {
            val prompt = """
                Kamu adalah asisten evaluasi permainan kognitif anak di aplikasi FocusPlay.
                Nilai hasil bermain anak berdasarkan data sesi ini:
                - Nama anak: $namaAnak
                - Nama game: $namaGame
                - Skor: $skor
                - Akurasi: $akurasi%
                - Durasi bermain: $durasiDetik detik
                - Fase akhir: $fase

                Tulis dalam Bahasa Indonesia, hangat, jelas, dan mudah dipahami orang tua.
                Maksimal 3 kalimat.
                Kalimat pertama menilai performa anak berdasarkan skor dan akurasi.
                Kalimat kedua memberi saran pendampingan praktis.
                Jika akurasi tinggi, beri apresiasi dan boleh sarankan tantangan lebih sulit.
                Jika akurasi rendah, sarankan latihan pelan-pelan tanpa menyalahkan anak.
                Jangan gunakan bullet, markdown, atau judul.
            """.trimIndent()

            val connection = (URL(FREEMODEL_CHAT_URL).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Authorization", "Bearer $apiKey")
                setRequestProperty("Content-Type", "application/json")
                connectTimeout = 15000
                readTimeout = 15000
                doOutput = true
            }

            val jsonBody = JSONObject().apply {
                put("model", MODEL_AI)
                put(
                    "messages",
                    JSONArray().put(
                        JSONObject().apply {
                            put("role", "user")
                            put("content", prompt)
                        }
                    )
                )
            }

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(jsonBody.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseString = BufferedReader(InputStreamReader(connection.inputStream)).use {
                    it.readText()
                }
                val jsonResponse = JSONObject(responseString)
                jsonResponse
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                    .trim()
            } else {
                val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                Log.e("EVALUASI_AI", "FreeModel gagal. HTTP $responseCode: $errorBody")
                buatEvaluasiCadangan(skor, akurasi, fase)
            }
        } catch (e: Exception) {
            Log.e("EVALUASI_AI", "Gagal memanggil model $MODEL_AI: ${e.message}")
            buatEvaluasiCadangan(skor, akurasi, fase)
        }
    }

    private fun buatEvaluasiCadangan(skor: Int, akurasi: Int, fase: Int): String {
        return when {
            akurasi >= 80 -> "$namaAnak menunjukkan fokus yang baik di game $namaGame dengan skor $skor dan akurasi $akurasi%. Orang tua dapat memberi tantangan bertahap sambil tetap menjaga durasi bermain tetap nyaman."
            akurasi >= 60 -> "$namaAnak sudah berusaha cukup baik di game $namaGame dengan akurasi $akurasi% pada fase $fase. Dampingi dengan latihan singkat dan beri pujian saat anak berhasil mengikuti urutan dengan benar."
            else -> "$namaAnak masih perlu latihan pelan-pelan di game $namaGame karena akurasi sesi ini $akurasi%. Coba ulangi permainan dengan suasana tenang dan bantu anak mengenali pola sebelum meningkatkan tantangan."
        }
    }

    private fun aturTombol() {
        val mainLagi: () -> Unit = {
            startActivity(Intent(this, GameDescriptionActivity::class.java).apply {
                putExtra("ID_ANAK", idAnak)
                putExtra("NAMA_ANAK", namaAnak)
                putExtra("USIA_ANAK", usiaAnak)
                putExtra("GAME_KEY", gameKey)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            })
            finish()
        }

        findViewById<TextView>(R.id.btnMainGameLagiAtas).jadiTombol(mainLagi)
        findViewById<TextView>(R.id.btnMainLagi).jadiTombol(mainLagi)
        findViewById<TextView>(R.id.btnPilihGameLain).jadiTombol { bukaDashboard() }
        findViewById<TextView>(R.id.btnLihatDashboard).jadiTombol { bukaDashboard() }
    }

    private fun bukaDashboard() {
        startActivity(Intent(this, DashboardAnakActivity::class.java).apply {
            putExtra("ID_ANAK", idAnak)
            putExtra("NAMA_ANAK", namaAnak)
            putExtra("USIA_ANAK", usiaAnak)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        })
        finish()
    }

    private fun View.jadiTombol(onClick: () -> Unit) {
        setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    view.animate().scaleX(0.96f).scaleY(0.96f).setDuration(45).start()
                    true
                }
                MotionEvent.ACTION_UP -> {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(55)
                        .withEndAction(onClick).start()
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(55).start()
                    true
                }
                else -> true
            }
        }
    }
}
