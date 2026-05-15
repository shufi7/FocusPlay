package com.example.focusplay.network

import com.example.focusplay.model.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    // Sesuai dengan alamat file PHP kita
    @POST("controllers/login_pendamping.php")
    fun loginPendamping(@Body request: HashMap<String, String>): Call<LoginResponse>
}