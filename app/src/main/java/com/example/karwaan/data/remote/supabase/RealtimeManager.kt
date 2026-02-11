package com.example.karwaan.data.remote.supabase

import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onStart

class RealtimeManager {
    private val supabase = SupabaseProvider.client

    fun observeMembers(tripId: String): Flow<Unit> {
        val channel = supabase.realtime.channel("trip-$tripId")

        return channel
            .postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "members"
                filter = "trip_id=eq.$tripId"
            }
            .onStart {
                channel.subscribe(blockUntilSubscribed = true)
            }
            .map { Unit }
    }

}