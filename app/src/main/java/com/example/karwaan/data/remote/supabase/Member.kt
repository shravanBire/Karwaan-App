package com.example.karwaan.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Member(
    val id: String? = null,
    val trip_id: String,
    val user_id: String,
    val display_name: String,
    val marker_color: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val last_updated: String? = null,
    val is_active: Boolean? = true
)
