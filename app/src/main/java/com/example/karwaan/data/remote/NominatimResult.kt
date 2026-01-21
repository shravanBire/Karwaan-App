package com.example.karwaan.data.remote

import com.google.gson.annotations.SerializedName

data class NominatimResult(
    @SerializedName("display_name")
    val displayName: String,

    @SerializedName("lat")
    val latitude: String,

    @SerializedName("lon")
    val longitude: String
)
