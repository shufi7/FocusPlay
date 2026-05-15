package com.example.focusplay.model

// --- Model untuk balasan Login ---
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

// --- Model untuk balasan Register ---
data class RegisterResponse(
    val status: String? = null,
    val message: String
)