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
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.Point

import com.example.karwaan.R

import org.maplibre.android.style.layers.PropertyFactory.*



private const val USER_LOCATION_SOURCE_ID = "user-location-source"
private const val USER_LOCATION_LAYER_ID = "user-location-layer"
private const val USER_LOCATION_ICON_ID = "user-location-icon"


private const val DESTINATION_SOURCE_ID = "destination-source"
private const val DESTINATION_LAYER_ID = "destination-layer"
private const val DESTINATION_ICON_ID = "destination-icon"


@SuppressLint("MissingPermission")
@Composable
fun MapViewContainer(
    modifier: Modifier = Modifier,
    searchedLocation: SearchResult?,
    userLocation: UserLocation?,
    recenterRequestId: Int
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    var map by remember { mutableStateOf<MapLibreMap?>(null) }

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
                    ) { style ->

                        style.addImage(
                            USER_LOCATION_ICON_ID,
                            context.getDrawable(R.drawable.user_location_dot)!!
                        )

                        // 🔴 DESTINATION (red pin)
                        style.addImage(
                            DESTINATION_ICON_ID,
                            context.getDrawable(
                                org.maplibre.android.R.drawable.maplibre_marker_icon_default
                            )!!
                        )
                    }
                }
            }
        }
    )

    // 🔵 Update / move blue dot when user location changes
    LaunchedEffect(userLocation) {
        userLocation?.let { loc ->
            map?.let { mapLibre ->
                updateUserLocation(mapLibre, loc.latitude, loc.longitude)
            }
        }
    }
    LaunchedEffect(searchedLocation) {
        searchedLocation?.let { dest ->
            map?.let { mapLibre ->
                updateDestinationLocation(
                    mapLibre,
                    dest.latitude,
                    dest.longitude
                )
            }
        }
    }

    // 🎯 Recenter camera when GPS button is pressed
    LaunchedEffect(userLocation, recenterRequestId) {
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

    // 🎯 Animate camera when searched location changes
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

private fun updateUserLocation(
    map: MapLibreMap,
    latitude: Double,
    longitude: Double
) {
    val style = map.style ?: return

    val point = Point.fromLngLat(longitude, latitude)
    val feature = Feature.fromGeometry(point)

    val source = style.getSourceAs<GeoJsonSource>(USER_LOCATION_SOURCE_ID)

    if (source == null) {
        // Create source
        style.addSource(
            GeoJsonSource(
                USER_LOCATION_SOURCE_ID,
                feature
            )
        )

        // Create blue dot layer
        style.addLayer(
            SymbolLayer(
                USER_LOCATION_LAYER_ID,
                USER_LOCATION_SOURCE_ID
            ).withProperties(
                iconImage(USER_LOCATION_ICON_ID),
                iconSize(1.2f),
                iconIgnorePlacement(true),
                iconAllowOverlap(true)
            )
        )
    } else {
        // Update existing location
        source.setGeoJson(feature)
    }
}
private fun updateDestinationLocation(
    map: MapLibreMap,
    latitude: Double,
    longitude: Double
) {
    val style = map.style ?: return

    val point = Point.fromLngLat(longitude, latitude)
    val feature = Feature.fromGeometry(point)

    val source = style.getSourceAs<GeoJsonSource>(DESTINATION_SOURCE_ID)

    if (source == null) {
        // First time: create source + layer
        style.addSource(
            GeoJsonSource(
                DESTINATION_SOURCE_ID,
                feature
            )
        )

        style.addLayer(
            SymbolLayer(
                DESTINATION_LAYER_ID,
                DESTINATION_SOURCE_ID
            ).withProperties(
                iconImage(DESTINATION_ICON_ID),
                iconSize(1.3f),
                iconAllowOverlap(true),
                iconIgnorePlacement(true)
            )
        )
    } else {
        // Update destination position
        source.setGeoJson(feature)
    }
}

