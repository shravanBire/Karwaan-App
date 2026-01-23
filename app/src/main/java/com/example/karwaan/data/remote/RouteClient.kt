package com.example.karwaan.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RouteClient {

    private const val BASE_URL = "https://router.project-osrm.org/"

    val api: RouteApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RouteApi::class.java)
    }
}
