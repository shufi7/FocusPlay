package com.example.focusplay.utils

import android.app.Activity
import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import com.example.focusplay.BuildConfig
import com.example.focusplay.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
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

object GameResultHelper {

    private const val AI_CONNECT_TIMEOUT_MILLIS = 40000
    private const val AI_READ_TIMEOUT_MILLIS = 60000
    private const val FIRESTORE_SAVE_TIMEOUT_MILLIS = 10000L

    fun evaluasiDanSimpanRealtime(
        activity: Activity,
        idAnak: String,
        namaAnak: String,
        namaGame: String,
        skor: Int,
        akurasi: Int,
        durasiDetik: Int,
        durasiMenit: Int,
        faseAkhir: Int,
        onSelesai: (String) -> Unit
    ) {
        // MEMAKSA PEMBUATAN POP-UP DI MAIN THREAD INSTAN
        activity.runOnUiThread {
            if (activity.isFinishing || activity.isDestroyed) {
                jalankanProsesAI(
                    activity, idAnak, namaAnak, namaGame, skor, akurasi,
                    durasiDetik, durasiMenit, faseAkhir, null, onSelesai
                )
                return@runOnUiThread
            }

            try {
                val layout = LayoutInflater.from(activity)
                    .inflate(R.layout.dialog_loading_evaluasi, null, false)

                val loadingDialog = AlertDialog.Builder(activity)
                    .setView(layout)
                    .setCancelable(false)
                    .create()

                loadingDialog.show()
                loadingDialog.window?.apply {
                    setBackgroundDrawableResource(android.R.color.transparent)
                    setDimAmount(0.68f)
                    addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                    setLayout(
                        (activity.resources.displayMetrics.widthPixels * 0.9f).toInt(),
                        WindowManager.LayoutParams.WRAP_CONTENT
                    )
                }

                jalankanProsesAI(
                    activity, idAnak, namaAnak, namaGame, skor, akurasi,
                    durasiDetik, durasiMenit, faseAkhir, loadingDialog, onSelesai
                )
            } catch (e: Exception) {
                Log.e("UI_ERROR", "Gagal menampilkan dialog evaluasi: ${e.message}")
                jalankanProsesAI(
                    activity, idAnak, namaAnak, namaGame, skor, akurasi,
                    durasiDetik, durasiMenit, faseAkhir, null, onSelesai
                )
            }
        }
    }

    private fun jalankanProsesAI(
        activity: Activity,
        idAnak: String,
        namaAnak: String,
        namaGame: String,
        skor: Int,
        akurasi: Int,
        durasiDetik: Int,
        durasiMenit: Int,
        faseAkhir: Int,
        loadingDialog: AlertDialog?,
        onSelesai: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            var hasilAI = "Wah, $namaAnak sangat fokus di game $namaGame! Saran untuk ortu: pertahankan durasi bermain ini agar konsentrasinya stabil."
            
            val apiKey = BuildConfig.OPENROUTER_API_KEY.trim()
            val model = BuildConfig.OPENROUTER_MODEL.trim().ifBlank { "openai/gpt-4o-mini" }

            if (apiKey.isNotBlank()) {
                var attempt = 0
                val maxAttempts = 2
                var success = false

                while (attempt < maxAttempts && !success) {
                    try {
                        if (attempt > 0) kotlinx.coroutines.delay(1500) // Delay sebelum coba lagi

                        val prompt = """
                            Kamu adalah asisten evaluasi permainan kognitif anak untuk aplikasi FocusPlay.
                            Buat evaluasi singkat berdasarkan data sesi berikut:
                            - Nama anak: $namaAnak
                            - Game: $namaGame
                            - Skor: $skor
                            - Akurasi: $akurasi%
                            - Durasi: $durasiDetik detik ($durasiMenit menit)
                            - Fase akhir: $faseAkhir
        
                            Tulis dalam Bahasa Indonesia, ramah untuk orang tua, maksimal 3 kalimat.
                            Nilai performa anak berdasarkan skor, akurasi, durasi, dan fase akhir.
                            Berikan apresiasi yang realistis dan satu saran pendampingan praktis.
                            Jangan pakai bullet, markdown, atau pembuka seperti "Berikut evaluasinya".
                        """.trimIndent()

                        val url = URL("https://openrouter.ai/api/v1/chat/completions")
                        val connection = url.openConnection() as HttpURLConnection
                        connection.requestMethod = "POST"
                        connection.setRequestProperty("Authorization", "Bearer $apiKey")
                        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                        connection.setRequestProperty("Accept", "application/json")
                        connection.setRequestProperty("HTTP-Referer", "https://focusplay.app")
                        connection.setRequestProperty("X-Title", "FocusPlay")
                        connection.connectTimeout = AI_CONNECT_TIMEOUT_MILLIS
                        connection.readTimeout = AI_READ_TIMEOUT_MILLIS
                        connection.doOutput = true

                        val jsonBody = JSONObject().apply {
                            put("model", model)
                            put("messages", JSONArray().put(JSONObject().apply {
                                put("role", "user")
                                put("content", prompt)
                            }))
                            put("temperature", 0.7)
                            put("max_tokens", 250)
                        }

                        OutputStreamWriter(connection.outputStream, "UTF-8").use { 
                            it.write(jsonBody.toString())
                        }

                        val responseCode = connection.responseCode
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            val responseString = connection.inputStream.bufferedReader().use { it.readText() }
                            val jsonResponse = JSONObject(responseString)
                            
                            val choices = jsonResponse.optJSONArray("choices")
                            if (choices != null && choices.length() > 0) {
                                val content = choices.getJSONObject(0)
                                    .optJSONObject("message")
                                    ?.optString("content")
                                
                                if (!content.isNullOrBlank()) {
                                    hasilAI = content.trim()
                                    success = true
                                    Log.i("AI_SUCCESS", "Respon AI berhasil didapat pada percobaan ${attempt + 1}")
                                }
                            }
                        } else {
                            val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() }
                            Log.e("AI_ERROR", "Percobaan ${attempt + 1} gagal (HTTP $responseCode): $errorBody")
                        }
                    } catch (e: Exception) {
                        Log.e("AI_ERROR", "Percobaan ${attempt + 1} error: ${e.message}")
                    }
                    attempt++
                }
            } else {
                Log.e("AI_ERROR", "OPENROUTER_API_KEY kosong di BuildConfig.")
            }

            // 2. Simpan Dokumen Rekaman ke Cloud Firestore
            try {
                val auth = FirebaseAuth.getInstance()
                val uid = auth.currentUser?.uid
                if (uid != null) {
                    val formatTanggal = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                    val dataRiwayat = hashMapOf(
                        "id_pendamping" to uid,
                        "id_anak" to idAnak,
                        "nama_anak" to namaAnak,
                        "nama_game" to namaGame,
                        "skor" to skor,
                        "akurasi" to akurasi,
                        "durasi_detik" to durasiDetik,
                        "durasi_menit" to durasiMenit,
                        "fase_akhir" to faseAkhir,
                        "tanggal" to formatTanggal.format(Date()),
                        "timestamp" to System.currentTimeMillis(),
                        "evaluasi_ai" to hasilAI
                    )
                    val tersimpan = withTimeoutOrNull(FIRESTORE_SAVE_TIMEOUT_MILLIS) {
                        FirebaseFirestore.getInstance().collection("tb_riwayat").add(dataRiwayat).await()
                        true
                    } ?: false

                    if (!tersimpan) {
                        Log.e("FIRESTORE_ERROR", "Menyimpan riwayat terlalu lama, halaman evaluasi tetap dibuka.")
                    }
                }
            } catch (e: Exception) {
                Log.e("FIRESTORE_ERROR", "Gagal menyimpan data: ${e.message}")
            }

            // 3. Selesai Memproses, Tutup Pop-Up dan Berpindah ke Halaman Evaluasi
            withContext(Dispatchers.Main) {
                if (loadingDialog != null && loadingDialog.isShowing && !activity.isFinishing && !activity.isDestroyed) {
                    try {
                        loadingDialog.dismiss()
                    } catch (e: Exception) {
                        Log.e("UI_ERROR", "Gagal menghentikan pop-up dialog: ${e.message}")
                    }
                }

                // Pindah ke layar EvaluasiActivity
                onSelesai(hasilAI)
            }
        }
    }
}
