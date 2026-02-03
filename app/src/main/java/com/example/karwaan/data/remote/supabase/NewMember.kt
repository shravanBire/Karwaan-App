package com.example.karwaan.data.remote.supabase

import kotlinx.serialization.Serializable

@Serializable
data class NewMember(
    val trip_id: String,
    val user_id: String,
    val display_name: String,
    val marker_color: String
)