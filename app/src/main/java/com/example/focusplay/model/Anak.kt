package com.example.focusplay.model

// Tambahkan nilai default (seperti = "" atau = 0) agar Firebase
// tidak bingung saat menarik data kosong dari database.
data class Anak(
    var id_dokumen: String = "",
    var id_pendamping: String = "",
    var nama_anak: String = "",
    var umur: Int = 0
)