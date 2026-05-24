package com.example.focusplay.utils

import android.app.Activity
import android.app.ProgressDialog
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object GameResultHelper {

    // Masukkan API Key kamu di sini nantinya
    private const val FREEMODEL_API_KEY = "fe_0a_1b4e26927d2353545e6de41533c50dc6446a02..."

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
        // 1. Munculkan Dialog Loading
        val loadingDialog = ProgressDialog(activity).apply {
            setMessage("Hebat! AI sedang menyusun evaluasimu...")
            setCancelable(false)
            show()
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 2. Siapkan Prompt untuk AI
                val prompt = "Anak bernama $namaAnak baru saja selesai bermain game kognitif '$namaGame'. " +
                        "Dia mendapatkan skor $skor dengan akurasi $akurasi% dalam waktu $durasiMenit menit. " +
                        "Tolong berikan 1 kalimat pujian singkat untuk anak, dan 1 kalimat evaluasi ringkas untuk orang tua."

                // 3. Simulasi balasan AI (Sebelum dihubungkan ke API Gemini asli)
                val hasilAI = "Wah, $namaAnak sangat fokus di game $namaGame! Saran untuk ortu: pertahankan durasi bermain ini agar konsentrasinya stabil."

                // 4. Siapkan Data untuk disatukan ke Firestore
                val auth = FirebaseAuth.getInstance()
                val db = FirebaseFirestore.getInstance()
                val uid = auth.currentUser?.uid ?: return@launch

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

                // 5. Simpan ke Firestore
                db.collection("tb_riwayat").add(dataRiwayat).await()

                // 6. Kembali ke Main Thread untuk menutup loading dan pindah halaman
                withContext(Dispatchers.Main) {
                    loadingDialog.dismiss()
                    Toast.makeText(activity, "Evaluasi AI berhasil direkam!", Toast.LENGTH_SHORT).show()
                    onSelesai(hasilAI)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    loadingDialog.dismiss()
                    Toast.makeText(activity, "Gagal mengevaluasi: ${e.message}", Toast.LENGTH_SHORT).show()
                    onSelesai("Maaf, AI gagal memuat evaluasi saat ini.")
                }
            }
        }
    }
}