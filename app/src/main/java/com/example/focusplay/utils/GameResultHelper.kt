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

/**
 * Mengurus hasil akhir game, evaluasi AI, dan penyimpanan Firestore.
 *
 * Data sesi dikirim dari Activity game ke evaluasiDanSimpanRealtime(). Setelah dapat evaluasi,
 * data disimpan ke "tb_riwayat", lalu hasil dikembalikan melalui callback onSelesai
 * supaya Activity game bisa membuka EvaluasiActivity.
 */
object GameResultHelper {

    // ==================== BAGIAN BATAS WAKTU PROSES ====================
    // Batas waktu membuat koneksi ke layanan AI.
    private const val AI_CONNECT_TIMEOUT_MILLIS = 40000
    // Batas waktu membaca respons layanan AI.
    private const val AI_READ_TIMEOUT_MILLIS = 60000
    // Batas waktu penyimpanan dokumen Firestore.
    private const val FIRESTORE_SAVE_TIMEOUT_MILLIS = 10000L

    // ==================== BAGIAN DIALOG DAN MULAI EVALUASI ====================
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
        // Dialog loading berasal dari dialog_loading_evaluasi.xml dan dibuat di main thread.
        // MEMAKSA PEMBUATAN POP-UP DI MAIN THREAD INSTAN
        // Memastikan proses pembuatan dialog berjalan pada UI thread.
        activity.runOnUiThread {
            // Tetap menjalankan proses tanpa dialog jika Activity sudah ditutup.
            if (activity.isFinishing || activity.isDestroyed) {
                jalankanProsesAI(
                    activity, idAnak, namaAnak, namaGame, skor, akurasi,
                    durasiDetik, durasiMenit, faseAkhir, null, onSelesai
                )
                return@runOnUiThread
            }

            try {
                // Mengambil layout dialog loading.
                val layout = LayoutInflater.from(activity)
                    .inflate(R.layout.dialog_loading_evaluasi, null, false)

                // Membuat dialog yang tidak dapat ditutup selama proses.
                val loadingDialog = AlertDialog.Builder(activity)
                    .setView(layout)
                    .setCancelable(false)
                    .create()

                // Menampilkan dialog sebelum request AI dimulai.
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

                // Menjalankan proses utama sambil membawa referensi dialog.
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

    // ==================== BAGIAN PROSES AI DAN FIRESTORE ====================
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
        // Proses jaringan dan Firestore dijalankan di thread IO agar tampilan tidak macet.
        CoroutineScope(Dispatchers.IO).launch {
            // Teks cadangan digunakan jika API tidak tersedia atau gagal.
            var hasilAI = "Wah, $namaAnak sangat fokus di game $namaGame! Saran untuk ortu: pertahankan durasi bermain ini agar konsentrasinya stabil."
            
            // Mengambil API key dan model dari BuildConfig.
            val apiKey = BuildConfig.OPENROUTER_API_KEY.trim()
            val model = BuildConfig.OPENROUTER_MODEL.trim().ifBlank { "openai/gpt-4o-mini" }

            // Request AI hanya dijalankan jika API key tersedia.
            if (apiKey.isNotBlank()) {
                var attempt = 0
                val maxAttempts = 2
                var success = false

                // Maksimal dua percobaan untuk memperoleh respons.
                while (attempt < maxAttempts && !success) {
                    try {
                        if (attempt > 0) kotlinx.coroutines.delay(1500) // Delay sebelum coba lagi

                        // Menyusun prompt berdasarkan statistik permainan.
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

                        // Membuka koneksi HTTP ke endpoint OpenRouter.
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

                        // Menyusun body JSON sesuai format chat completions.
                        val jsonBody = JSONObject().apply {
                            put("model", model)
                            put("messages", JSONArray().put(JSONObject().apply {
                                put("role", "user")
                                put("content", prompt)
                            }))
                            put("temperature", 0.7)
                            put("max_tokens", 250)
                        }

                        // Mengirim body JSON melalui output stream koneksi.
                        OutputStreamWriter(connection.outputStream, "UTF-8").use { 
                            it.write(jsonBody.toString())
                        }

                        // Membaca respons hanya jika status HTTP berhasil.
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
                                    // Menyimpan isi evaluasi sebagai hasil akhir.
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

            // Data yang tersimpan di sini nanti dibaca DashboardActivity untuk grafik dan recap AI.
            // 2. Simpan Dokumen Rekaman ke Cloud Firestore
            try {
                // Mengambil UID pendamping untuk menghubungkan riwayat dengan akun.
                val auth = FirebaseAuth.getInstance()
                val uid = auth.currentUser?.uid
                if (uid != null) {
                    val formatTanggal = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                    // Menyusun seluruh field dokumen riwayat permainan.
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
                    // Menyimpan dokumen dengan batas waktu agar halaman tidak menunggu selamanya.
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
            // Kembali ke main thread untuk menutup dialog dan menjalankan callback.
            withContext(Dispatchers.Main) {
                if (loadingDialog != null && loadingDialog.isShowing && !activity.isFinishing && !activity.isDestroyed) {
                    try {
                        loadingDialog.dismiss()
                    } catch (e: Exception) {
                        Log.e("UI_ERROR", "Gagal menghentikan pop-up dialog: ${e.message}")
                    }
                }

                // Mengirim hasil kembali ke Activity game untuk membuka EvaluasiActivity.
                // Pindah ke layar EvaluasiActivity
                onSelesai(hasilAI)
            }
        }
    }
}
