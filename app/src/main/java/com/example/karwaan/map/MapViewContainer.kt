package com.example.karwaan.map

import android.annotation.SuppressLint
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.Point
import org.maplibre.geojson.LineString
import com.example.karwaan.R
import kotlinx.coroutines.delay
import org.maplibre.android.style.layers.Property.LINE_CAP_ROUND
import org.maplibre.android.style.layers.Property.LINE_JOIN_ROUND

private const val USER_LOCATION_SOURCE_ID = "user-location-source"
private const val USER_LOCATION_LAYER_ID = "user-location-layer"
private const val USER_LOCATION_ICON_ID = "user-location-icon"

private const val DESTINATION_SOURCE_ID = "destination-source"
private const val DESTINATION_LAYER_ID = "destination-layer"
private const val DESTINATION_ICON_ID = "destination-icon"

private const val ROUTE_SOURCE_ID = "route-source"
private const val ROUTE_LAYER_ID = "route-layer"

@SuppressLint("MissingPermission")
@Composable
fun MapViewContainer(
    modifier: Modifier = Modifier,
    searchedLocation: SearchResult?,
    userLocation: UserLocation?,
    routePoints: List<Pair<Double, Double>>,
    recenterRequestId: Int,
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    var animatedRoutePoints by remember { mutableStateOf<List<Pair<Double, Double>>>(emptyList()) }


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

                        // 🔵 User location blue dot
                        style.addImage(
                            USER_LOCATION_ICON_ID,
                            context.getDrawable(R.drawable.user_location_dot)!!
                        )

                        // 🔴 Destination pin
                        style.addImage(
                            DESTINATION_ICON_ID,
                            context.getDrawable(
                                org.maplibre.android.R.drawable.maplibre_marker_icon_default
                            )!!
                        )
                    }

                    mapLibreMap.uiSettings.apply {
                        isCompassEnabled = true
                        setCompassMargins(0, 350, 32, 0)
                    }
                }
            }
        }
    )

    var hasAutoCentered by rememberSaveable { mutableStateOf(false) }

    // 🔵 USER LOCATION MARKER UPDATES (moves every 2–3 meters)
    // ✅ DOES NOT move camera
    LaunchedEffect(userLocation) {
        userLocation?.let {
            map?.let { m ->
                updateUserLocation(m, it.latitude, it.longitude)
            }
        }
    }

    // 🔴 DESTINATION MARKER
    LaunchedEffect(searchedLocation) {
        searchedLocation?.let {
            map?.let { m ->
                updateDestinationLocation(m, it.latitude, it.longitude)
            }
        }
    }

    //  ROUTE DRAWING (animated)
    LaunchedEffect(routePoints) {
        if (routePoints.isEmpty()) return@LaunchedEffect

        animatedRoutePoints = emptyList()
        for (i in 1..routePoints.size) {
            animatedRoutePoints = routePoints.take(i)
            delay(2L)
        }
    }

    LaunchedEffect(animatedRoutePoints) {
        if (animatedRoutePoints.isNotEmpty()) {
            map?.let { m ->
                updateRoute(m, animatedRoutePoints)
            }
        }
    }

    LaunchedEffect(routePoints) {
        map?.let { m ->
            if (routePoints.isEmpty()) {
                clearRoute(m)   // 🔥 THIS IS THE KEY
            } else {
                fitRouteInCamera(m, routePoints)
            }
        }
    }

    // 🎯 MANUAL RECENTER (button click)
    LaunchedEffect(recenterRequestId) {
        val m = map ?: return@LaunchedEffect
        val loc = userLocation ?: return@LaunchedEffect

        m.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(loc.latitude, loc.longitude),
                15.5
            ),
            1000
        )
    }


    LaunchedEffect(map, userLocation) {
        val m = map ?: return@LaunchedEffect
        val loc = userLocation ?: return@LaunchedEffect
        if (hasAutoCentered) return@LaunchedEffect

        m.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(loc.latitude, loc.longitude),
                15.5
            ),
            1000
        )

        hasAutoCentered = true   // 🔥 THIS WAS MISSING
    }

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



private fun updateUserLocation(map: MapLibreMap, latitude: Double, longitude: Double) {
    val style = map.style ?: return
    val feature = Feature.fromGeometry(Point.fromLngLat(longitude, latitude))

    val source = style.getSourceAs<GeoJsonSource>(USER_LOCATION_SOURCE_ID)
    if (source == null) {
        style.addSource(GeoJsonSource(USER_LOCATION_SOURCE_ID, feature))
        style.addLayer(
            SymbolLayer(USER_LOCATION_LAYER_ID, USER_LOCATION_SOURCE_ID).withProperties(
                iconImage(USER_LOCATION_ICON_ID),
                iconSize(1.2f),
                iconAllowOverlap(true),
                iconIgnorePlacement(true)
            )
        )
    } else {
        source.setGeoJson(feature)
    }
}

private fun updateDestinationLocation(map: MapLibreMap, latitude: Double, longitude: Double) {
    val style = map.style ?: return
    val feature = Feature.fromGeometry(Point.fromLngLat(longitude, latitude))

    val source = style.getSourceAs<GeoJsonSource>(DESTINATION_SOURCE_ID)
    if (source == null) {
        style.addSource(GeoJsonSource(DESTINATION_SOURCE_ID, feature))
        style.addLayer(
            SymbolLayer(DESTINATION_LAYER_ID, DESTINATION_SOURCE_ID).withProperties(
                iconImage(DESTINATION_ICON_ID),
                iconSize(1.3f),
                iconAllowOverlap(true),
                iconIgnorePlacement(true)
            )
        )
    } else {
        source.setGeoJson(feature)
    }
}

private fun fitRouteInCamera(map: MapLibreMap, points: List<Pair<Double, Double>>) {
    val boundsBuilder = org.maplibre.android.geometry.LatLngBounds.Builder()
    points.forEach { boundsBuilder.include(LatLng(it.first, it.second)) }
    map.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 320), 1200)
}

private fun updateRoute(map: MapLibreMap, points: List<Pair<Double, Double>>) {
    val style = map.style ?: return
    val lineString = LineString.fromLngLats(points.map { Point.fromLngLat(it.second, it.first) })

    val source = style.getSourceAs<GeoJsonSource>(ROUTE_SOURCE_ID)
    if (source == null) {
        style.addSource(GeoJsonSource(ROUTE_SOURCE_ID, lineString))
        style.addLayer(
            LineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID).withProperties(
                lineColor("#1A73E8"),
                lineWidth(6f),
                lineCap(LINE_CAP_ROUND),
                lineJoin(LINE_JOIN_ROUND)
            )
        )
    } else {
        source.setGeoJson(lineString)
    }
}


private fun clearRoute(map: MapLibreMap) {
    val style = map.style ?: return

    style.getLayer(ROUTE_LAYER_ID)?.let {
        style.removeLayer(ROUTE_LAYER_ID)
    }

    style.getSource(ROUTE_SOURCE_ID)?.let {
        style.removeSource(ROUTE_SOURCE_ID)
    }
}

