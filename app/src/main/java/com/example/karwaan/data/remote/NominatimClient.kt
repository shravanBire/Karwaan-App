package com.example.karwaan.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient


object NominatimClient {

    private const val BASE_URL = "https://nominatim.openstreetmap.org/"

    val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "KarwaanApp/1.0 (shravanbire.144@gmail.com)")
                .build()
            chain.proceed(request)
        }
        .build()

    val api: NominatimApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NominatimApi::class.java)
    }


}