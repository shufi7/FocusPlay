package com.example.focusplay.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object GameResultHelper {
    fun simpanHasilPermainan(
        idAnak: String,
        namaAnak: String,
        namaGame: String,
        skor: Int,
        akurasi: Int,
        durasiMenit: Int
    ) {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val uid = auth.currentUser?.uid ?: return

        // Format tanggal agar rapi dibaca di Riwayat
        val formatTanggal = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        val tanggalSekarang = formatTanggal.format(Date())
        val timestamp = System.currentTimeMillis() // Untuk mengurutkan dari yang terbaru

        val dataRiwayat = hashMapOf(
            "id_pendamping" to uid,
            "id_anak" to idAnak,
            "nama_anak" to namaAnak,
            "nama_game" to namaGame,
            "skor" to skor,
            "akurasi" to akurasi,
            "durasi_menit" to durasiMenit,
            "tanggal" to tanggalSekarang,
            "timestamp" to timestamp
        )

        // Simpan ke koleksi baru bernama "tb_riwayat"
        db.collection("tb_riwayat").add(dataRiwayat)
    }
}