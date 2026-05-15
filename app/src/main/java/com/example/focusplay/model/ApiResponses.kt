package com.example.focusplay.model

// --- Model yang sudah ada sebelumnya ---
data class LoginResponse(
    val status: String,
    val message: String,
    val data: PendampingData? = null
)

data class PendampingData(
    val id_pendamping: Int,
    val nama_pendamping: String,
    val email: String,
    val peran: String
)

data class RegisterResponse(
    val status: String? = null,
    val message: String
)

// --- TAMBAHKAN INI DI BAGIAN PALING BAWAH ---
data class TambahAnakResponse(
    val status: String,
    val message: String
)