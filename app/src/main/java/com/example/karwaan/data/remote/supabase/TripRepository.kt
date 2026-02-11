package com.example.karwaan.data.remote.supabase

import com.example.karwaan.data.model.Trip
import io.github.jan.supabase.postgrest.from

class TripRepository {

    private val supabase = SupabaseProvider.client

    private fun randomColor() =
        listOf("#E53935", "#1E88E5", "#43A047", "#FB8C00").random()

    suspend fun createTrip(
        hostId: String,
        displayName: String
    ): Trip {
        val code = (100000..999999).random()

        val newTrip = NewTrip(
            trip_code = code,
            host_id = hostId
        )

        val insertedTrip =
            supabase
                .from("trips")
                .insert(newTrip) {
                    select()
                }
                .decodeSingle<Trip>()

        joinTripInternal(
            tripId = insertedTrip.id,
            userId = hostId,
            displayName = displayName
        )

        return insertedTrip
    }

    suspend fun joinTrip(
        tripCode: Int,
        userId: String,
        displayName: String
    ): Trip {

        val trip =
            supabase
                .from("trips")
                .select {
                    filter {
                        eq("trip_code", tripCode)
                        eq("is_active", true)
                    }
                }
                .decodeSingle<Trip>()

        joinTripInternal(
            tripId = trip.id,
            userId = userId,
            displayName = displayName
        )

        return trip
    }

    private suspend fun joinTripInternal(
        tripId: String,
        userId: String,
        displayName: String
    ) {
        val member = NewMember(
            trip_id = tripId,
            user_id = userId,
            display_name = displayName,
            marker_color = randomColor()
        )

        supabase
            .from("members")
            .insert(member)
    }

    suspend fun updateLocation(
        tripId: String,
        userId: String,
        lat: Double,
        lng: Double
    ) {
        supabase
            .from("members")
            .update(
                mapOf(
                    "latitude" to lat,
                    "longitude" to lng
                )
            ) {
                filter {
                    eq("trip_id", tripId)
                    eq("user_id", userId)
                }
            }
    }

    suspend fun leaveTrip(
        tripId: String,
        userId: String
    ) {
        supabase
            .from("members")
            .delete {
                filter {
                    eq("trip_id", tripId)
                    eq("user_id", userId)
                }
            }
    }
}