package com.example.karwaan.data.remote.supabase

import android.util.Log
import io.github.jan.supabase.postgrest.from
import java.util.Date

class TripRepository {
    private val client = SupabaseProvider.client

    suspend fun createTrip(hostId: String, displayName: String): Trip {
        // Generate 6-digit code
        val code = (100000..999999).random()

        // Create trip
        val trip = client.from("trips")
            .insert(NewTrip(
                trip_code = code,
                host_id = hostId,
                is_active = true
            ))
            .decodeSingle<Trip>()

        // Add host as member with random color
        val color = generateRandomColor()
        client.from("members").insert(NewMember(
            trip_id = trip.id,
            user_id = hostId,
            display_name = displayName.take(10),
            marker_color = color
        ))

        return trip
    }

    suspend fun joinTrip(tripCode: Int, userId: String, displayName: String): Trip {
        // Find trip
        val trip = client.from("trips")
            .select {
                filter {
                    eq("trip_code", tripCode)
                    eq("is_active", true)
                }
            }
            .decodeSingleOrNull<Trip>() ?: throw Exception("Trip not found")

        // Check if already member
        val existing = client.from("members")
            .select {
                filter {
                    eq("trip_id", trip.id)
                    eq("user_id", userId)
                }
            }
            .decodeList<Member>()
            .firstOrNull()

        if (existing != null) {
            // Re-activate if exists
            client.from("members")
                .update({
                    set("is_active", true)
                    set("display_name", displayName.take(10))
                }) {
                    filter {
                        eq("id", existing.id!!)
                    }
                }
        } else {
            // Add new member
            val color = generateRandomColor()
            client.from("members").insert(NewMember(
                trip_id = trip.id,
                user_id = userId,
                display_name = displayName.take(10),
                marker_color = color
            ))
        }

        return trip
    }

    suspend fun leaveTrip(tripId: String, userId: String) {
        client.from("members")
            .update({ set("is_active", false) }) {
                filter {
                    eq("trip_id", tripId)
                    eq("user_id", userId)
                }
            }
    }

    suspend fun updateLocation(tripId: String, userId: String, lat: Double, lng: Double) {
        client.from("members")
            .update({
                set("latitude", lat)
                set("longitude", lng)
                set("last_updated", Date().toString())
            }) {
                filter {
                    eq("trip_id", tripId)
                    eq("user_id", userId)
                }
            }
    }

    private fun generateRandomColor(): String {
        val colors = listOf(
            "#FF0000", "#00FF00", "#0000FF", "#FFFF00",
            "#FF00FF", "#00FFFF", "#FFA500", "#800080",
            "#FFC0CB", "#A52A2A", "#808080", "#000000"
        )
        return colors.random()
    }
}
