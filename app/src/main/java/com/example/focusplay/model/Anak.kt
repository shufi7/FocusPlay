package com.example.focusplay.model

import com.google.firebase.firestore.Exclude

data class Anak(
    @get:Exclude var id_dokumen: String = "", // Tambahan wajib untuk fitur hapus
    val id_pendamping: String = "",
    val nama_anak: String = "",
    val usia: Int = 0
)