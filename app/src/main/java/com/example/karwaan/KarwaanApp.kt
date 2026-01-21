package com.example.karwaan

import android.app.Application
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer

class KarwaanApp: Application() {

    override fun onCreate() {
        super.onCreate()

        // REQUIRED before using MapView
        MapLibre.getInstance(
            this,
            null, // no API key needed
            WellKnownTileServer.MapLibre
        )
    }
}