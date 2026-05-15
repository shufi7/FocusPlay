package com.example.focusplay.model

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