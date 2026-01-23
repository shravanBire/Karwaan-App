package com.example.karwaan.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RouteApi {

    @GET("route/v1/driving/{coords}")
    suspend fun getRoute(
        @Path("coords") coords: String,
        @Query("overview") overview: String = "full",
        @Query("geometries") geometries: String = "geojson"
    ): RouteResponse
}
