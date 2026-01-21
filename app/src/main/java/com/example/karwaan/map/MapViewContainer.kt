package com.example.karwaan.map

import android.annotation.SuppressLint
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.karwaan.utils.SearchResult
import com.example.karwaan.utils.UserLocation
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

@SuppressLint("MissingPermission")
@Composable
fun MapViewContainer(
    modifier: Modifier = Modifier,
    searchedLocation: SearchResult?,
    userLocation: UserLocation?
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    var map by remember { mutableStateOf<MapLibreMap?>(null) }

    LaunchedEffect(userLocation) {
        userLocation?.let {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(it.latitude, it.longitude),
                    15.5
                ),
                1000
            )
        }
    }


    AndroidView(
        modifier = modifier,
        factory = {
            mapView.apply {
                onCreate(null)
                getMapAsync { mapLibreMap ->
                    map = mapLibreMap
                    mapLibreMap.setStyle(
                        Style.Builder()
                            .fromUri("https://tiles.openfreemap.org/styles/liberty")
                    )
                }
            }
        }
    )

    // 🎯 Animate camera when destination changes
    LaunchedEffect(searchedLocation) {
        searchedLocation?.let {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(it.latitude, it.longitude),
                    14.5
                ),
                1200
            )
        }
    }
}
