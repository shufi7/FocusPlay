package com.example.focusplay.utils

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.example.focusplay.BuildConfig
import com.example.focusplay.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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

object GameResultHelper {

    fun evaluasiDanSimpanRealtime(
        activity: Activity,
        idAnak: String,
        namaAnak: String,
        namaGame: String,
        skor: Int,
        akurasi: Int,
        durasiMenit: Int,
        onSelesai: (String) -> Unit
    ) {
        // MEMAKSA PEMBUATAN POP-UP DI MAIN THREAD INSTAN
        activity.runOnUiThread {
            if (activity.isFinishing || activity.isDestroyed) {
                // Jika activity game ternyata sudah mati duluan, langsung jalankan AI di background tanpa pop-up
                jalankanProsesAI(activity, idAnak, namaAnak, namaGame, skor, akurasi, durasiMenit, null, onSelesai)
                return@runOnUiThread
            }

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

            // Jalankan komunikasi data dengan API FreeModel GPT-5.5
            jalankanProsesAI(activity, idAnak, namaAnak, namaGame, skor, akurasi, durasiMenit, loadingDialog, onSelesai)
        }
    }

    private fun jalankanProsesAI(
        activity: Activity,
        idAnak: String,
        namaAnak: String,
        namaGame: String,
        skor: Int,
        akurasi: Int,
        durasiMenit: Int,
        loadingDialog: AlertDialog?,
        onSelesai: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            var hasilAI = "Wah, $namaAnak sangat fokus di game $namaGame! Saran untuk ortu: pertahankan durasi bermain ini agar konsentrasinya stabil."
            var isApiBerhasil = false

            val apiKey = BuildConfig.FREEMODEL_API_KEY

            if (apiKey.isNotBlank()) try {
                val prompt = "Anak bernama $namaAnak baru saja selesai bermain game kognitif '$namaGame'. " +
                        "Dia mendapatkan skor $skor dengan akurasi $akurasi% dalam waktu $durasiMenit menit. " +
                        "Tolong berikan 1 kalimat pujian singkat yang ramah untuk anak, dan 1 kalimat evaluasi ringkas untuk orang tua. Langsung saja teks biasa."

                val url = URL("https://api.freemodel.dev/v1/chat/completions")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Authorization", "Bearer $apiKey")
                connection.setRequestProperty("Content-Type", "application/json")
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.doOutput = true

                val jsonBody = JSONObject()
                jsonBody.put("model", "gpt-5.5")

                val message = JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                }
                jsonBody.put("messages", JSONArray().put(message))

                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(jsonBody.toString())
                writer.flush()
                writer.close()

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val responseString = reader.readText()
                    reader.close()

                    val jsonResponse = JSONObject(responseString)
                    val choices = jsonResponse.getJSONArray("choices")
                    if (choices.length() > 0) {
                        hasilAI = choices.getJSONObject(0).getJSONObject("message").getString("content").trim()
                        isApiBerhasil = true
                    }
                }
            } catch (e: Exception) {
                Log.e("AI_ERROR", "Gagal memanggil API: ${e.message}")
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
                        "durasi_menit" to durasiMenit,
                        "tanggal" to formatTanggal.format(Date()),
                        "timestamp" to System.currentTimeMillis(),
                        "evaluasi_ai" to hasilAI
                    )
                    FirebaseFirestore.getInstance().collection("tb_riwayat").add(dataRiwayat).await()
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

                if (isApiBerhasil) {
                    Toast.makeText(activity, "Evaluasi AI berhasil direkam!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(activity, "Evaluasi AI menggunakan sistem cadangan.", Toast.LENGTH_SHORT).show()
                }

                // Pindah ke layar EvaluasiActivity
                onSelesai(hasilAI)
            }
        }
    }
}
