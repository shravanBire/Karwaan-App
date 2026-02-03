package com.example.karwaan.data.remote.supabase

import kotlinx.serialization.Serializable

@Serializable
data class Trip(
    val id: String,
    val trip_code: Int,
    val host_id: String,
    val is_active: Boolean? = true
)
