package com.example.focusplay.network

import com.example.focusplay.model.LoginResponse
import com.example.focusplay.model.RegisterResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("controllers/login_pendamping.php")
    fun loginPendamping(@Body request: HashMap<String, String>): Call<LoginResponse>

    @POST("controllers/register_pendamping.php")
    fun registerPendamping(@Body request: HashMap<String, String>): Call<RegisterResponse>

    @POST("controllers/tambah_anak.php")
    fun tambahAnak(@Body request: HashMap<String, Any>): Call<TambahAnakResponse>
}