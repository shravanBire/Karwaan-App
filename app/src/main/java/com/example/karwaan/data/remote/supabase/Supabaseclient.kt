package com.example.karwaan.data.remote.supabase

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json

object SupabaseProvider {
    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = "https://ryeqemimspkgxhlzvaay.supabase.co",
            supabaseKey = "sb_publishable_FKLnd95FRcOd0doozu6Vyw_uSeoGVVq"
        ) {
            install(Postgrest)
            install(Realtime)
            defaultSerializer = KotlinXSerializer(
                Json { ignoreUnknownKeys = true }
            )
        }
    }
}