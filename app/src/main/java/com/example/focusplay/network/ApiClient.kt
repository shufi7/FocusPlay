package com.example.focusplay.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    // Ingat! Ganti angka ini dengan IPv4 laptop kamu (misal: 192.168.1.15)
    private const val BASE_URL = "http://10.38.247.145:8000/"

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ApiService::class.java)
    }
}