package com.example.karwaan.data.remote.supabase

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RealtimeManager {
    private val supabase = SupabaseProvider.client

    fun observeMembers(tripId: String): Flow<List<Member>> = flow {
        // Fetch initial data
        val initial = supabase.from("members")
            .select {
                filter {
                    eq("trip_id", tripId)
                    eq("is_active", true)
                }
            }
            .decodeList<Member>()

        emit(initial)

        // Setup realtime channel
        val channel = supabase.realtime.channel("trip-$tripId")

        channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "members"
            filter = "trip_id=eq.$tripId"
        }.collect { action ->
            // Re-fetch on any change
            val updated = supabase.from("members")
                .select {
                    filter {
                        eq("trip_id", tripId)
                        eq("is_active", true)
                    }
                }
                .decodeList<Member>()

            emit(updated)
        }

        channel.subscribe(blockUntilSubscribed = true)
    }
}