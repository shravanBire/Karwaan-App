package com.example.karwaan.map

import android.annotation.SuppressLint
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

@SuppressLint("MissingPermission")
@Composable
fun MapViewContainer(
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            MapLibre.getInstance(context)

            MapView(context).apply {
                onCreate(null)
                getMapAsync { map ->
                    map.setStyle(Style.Builder().fromUri("https://tiles.openfreemap.org/styles/liberty"))
                }
            }
        },
        update = { mapView ->
            mapView.onResume()
        }
    )
}