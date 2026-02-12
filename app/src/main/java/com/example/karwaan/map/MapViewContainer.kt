package com.example.karwaan.map

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
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
import com.example.karwaan.data.model.Member
import kotlinx.coroutines.delay
import org.maplibre.android.style.layers.Property.LINE_CAP_ROUND
import org.maplibre.android.style.layers.Property.LINE_JOIN_ROUND
import org.maplibre.geojson.FeatureCollection
import org.maplibre.android.style.expressions.Expression.get
import org.maplibre.android.style.layers.Property

private const val USER_LOCATION_SOURCE_ID = "user-location-source"
private const val USER_LOCATION_LAYER_ID = "user-location-layer"
private const val USER_LOCATION_ICON_ID = "user-location-icon"
private const val DESTINATION_SOURCE_ID = "destination-source"
private const val DESTINATION_LAYER_ID = "destination-layer"
private const val DESTINATION_ICON_ID = "destination-icon"
private const val ROUTE_SOURCE_ID = "route-source"
private const val ROUTE_LAYER_ID = "route-layer"
private const val MEMBERS_SOURCE_ID = "members-source"
private const val MEMBERS_LAYER_ID = "members-layer"
private const val MEMBER_ICON_ID = "member-icon"

@SuppressLint("MissingPermission")
@Composable
fun MapViewContainer(
    modifier: Modifier = Modifier,
    searchedLocation: SearchResult?,
    userLocation: UserLocation?,
    routePoints: List<Pair<Double, Double>>,
    recenterRequestId: Int,
    tripMembers: List<Member>,
    currentUserId: String?
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    var animatedRoutePoints by remember { mutableStateOf<List<Pair<Double, Double>>>(emptyList()) }
    var styleReady by remember { mutableStateOf(false) }

    AndroidView(
        modifier = modifier,
        factory = {
            mapView.apply {
                onCreate(null)
                getMapAsync { mapLibreMap ->
                    map = mapLibreMap
                    mapLibreMap.setStyle(
                        Style.Builder().fromUri("https://tiles.openfreemap.org/styles/liberty")
                    ) { style ->

                        style.addImage(
                            USER_LOCATION_ICON_ID,
                            context.getDrawable(R.drawable.user_location_dot)!!
                        )
                        style.addImage(
                            DESTINATION_ICON_ID,
                            context.getDrawable(org.maplibre.android.R.drawable.maplibre_marker_icon_default)!!
                        )
                        // ✅ Use built-in MapLibre marker to rule out icon issues
                        style.addImage(
                            MEMBER_ICON_ID,
                            context.getDrawable(R.drawable.user_location_dot)!!
                        )

                        // ✅ Add members source + layer ONCE during style init
                        style.addSource(
                            GeoJsonSource(
                                MEMBERS_SOURCE_ID,
                                FeatureCollection.fromFeatures(emptyArray())
                            )
                        )
                        // Find the top-most layer in the style and add above it
                        val topLayerId = style.layers.lastOrNull()?.id
                        val membersLayer = SymbolLayer(MEMBERS_LAYER_ID, MEMBERS_SOURCE_ID)
                            .withProperties(
                                iconImage(get("icon_id")),
                                iconSize(1.0f),
                                iconAllowOverlap(true),
                                iconIgnorePlacement(true),
                                iconAnchor(Property.ICON_ANCHOR_CENTER)
                            )

                        if (topLayerId != null) {
                            style.addLayerAbove(membersLayer, topLayerId)
                        } else {
                            style.addLayer(membersLayer)
                        }

                        Log.d("MAP_DEBUG", "Top layer was: $topLayerId")

                        // ✅ Signal style is ready AFTER everything is added
                        styleReady = true

                        Log.d("MAP_DEBUG", "Style fully initialized")
                        Log.d("MAP_DEBUG", "Source exists: ${style.getSource(MEMBERS_SOURCE_ID) != null}")
                        Log.d("MAP_DEBUG", "Layer exists: ${style.getLayer(MEMBERS_LAYER_ID) != null}")
                        Log.d("MAP_DEBUG", "Image exists: ${style.getImage(MEMBER_ICON_ID) != null}")
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

    // 🔵 User location dot
    LaunchedEffect(userLocation) {
        userLocation?.let {
            map?.let { m -> updateUserLocation(m, it.latitude, it.longitude) }
        }
    }

    // 🔴 Destination marker
    LaunchedEffect(searchedLocation) {
        searchedLocation?.let {
            map?.let { m -> updateDestinationLocation(m, it.latitude, it.longitude) }
        }
    }

    // 👥 Trip members markers
    // ✅ Key fix: depend on styleReady AND tripMembers so it re-fires when either changes
    LaunchedEffect(styleReady, tripMembers) {
        if (!styleReady) {
            Log.d("MAP_DEBUG", "Style not ready yet, skipping members update")
            return@LaunchedEffect
        }
        val m = map ?: run {
            Log.d("MAP_DEBUG", "Map is null, skipping members update")
            return@LaunchedEffect
        }

        // ✅ Small delay to ensure MapLibre has fully committed the style
        delay(100)

        Log.d("MAP_DEBUG", "LaunchedEffect fired — styleReady=$styleReady, members=${tripMembers.size}, currentUserId=$currentUserId")

        updateMembersOnMap(
            map = m,
            members = tripMembers,
            currentUserId = currentUserId,
            context = context
        )
    }

    // 🛣 Route animation
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
            map?.let { m -> updateRoute(m, animatedRoutePoints) }
        }
    }

    LaunchedEffect(routePoints) {
        map?.let { m ->
            if (routePoints.isEmpty()) clearRoute(m)
            else fitRouteInCamera(m, routePoints)
        }
    }

    // 🎯 Manual recenter
    LaunchedEffect(recenterRequestId) {
        val m = map ?: return@LaunchedEffect
        val loc = userLocation ?: return@LaunchedEffect
        m.animateCamera(
            CameraUpdateFactory.newLatLngZoom(LatLng(loc.latitude, loc.longitude), 15.5),
            1000
        )
    }

    // 🎯 Auto-center once on first location
    LaunchedEffect(map, userLocation) {
        val m = map ?: return@LaunchedEffect
        val loc = userLocation ?: return@LaunchedEffect
        if (hasAutoCentered) return@LaunchedEffect

        m.animateCamera(
            CameraUpdateFactory.newLatLngZoom(LatLng(loc.latitude, loc.longitude), 15.5),
            1000
        )
        hasAutoCentered = true
    }

    // 🎯 Camera to searched location
    LaunchedEffect(searchedLocation) {
        searchedLocation?.let {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 14.5),
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
    style.getLayer(ROUTE_LAYER_ID)?.let { style.removeLayer(ROUTE_LAYER_ID) }
    style.getSource(ROUTE_SOURCE_ID)?.let { style.removeSource(ROUTE_SOURCE_ID) }
}

// ✅ Cleaned up updateMembersOnMap — removed camera animation (handled elsewhere)
private fun updateMembersOnMap(
    map: MapLibreMap,
    members: List<Member>,
    currentUserId: String?,
    context: Context
) {
    val style = map.style ?: run {
        Log.d("MAP_DEBUG", "❌ Style is null")
        return
    }

    val validMembers = members.filter {
        it.user_id != currentUserId &&
                it.latitude != null &&
                it.longitude != null
    }

    Log.d("MAP_DEBUG", "Valid members: ${validMembers.size}")

    val features = validMembers.map { member ->

        val iconId = "member_${member.user_id}"

        // Add colored icon only once
        if (style.getImage(iconId) == null) {
            val bitmap = createColoredMarkerBitmap(context, member.marker_color)
            style.addImage(iconId, bitmap)
            Log.d("MAP_DEBUG", "Added image for $iconId")
        }

        Feature.fromGeometry(
            Point.fromLngLat(member.longitude!!, member.latitude!!)
        ).apply {
            addStringProperty("icon_id", iconId)
        }
    }

    val source = style.getSourceAs<GeoJsonSource>(MEMBERS_SOURCE_ID)
    if (source == null) {
        Log.d("MAP_DEBUG", "❌ Members source missing")
        return
    }

    source.setGeoJson(FeatureCollection.fromFeatures(features))

    Log.d("MAP_DEBUG", "GeoJson updated with ${features.size} features")
}

private fun createColoredMarkerBitmap(
    context: android.content.Context,
    colorHex: String
): android.graphics.Bitmap {

    val size = 80
    val bitmap = android.graphics.Bitmap.createBitmap(
        size,
        size,
        android.graphics.Bitmap.Config.ARGB_8888
    )

    val canvas = android.graphics.Canvas(bitmap)
    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        style = android.graphics.Paint.Style.FILL
        color = android.graphics.Color.parseColor(colorHex)
    }

    // Draw circle
    canvas.drawCircle(size / 2f, size / 2f, size / 2.5f, paint)

    // White border
    val borderPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = 6f
        color = android.graphics.Color.WHITE
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2.5f, borderPaint)

    return bitmap
}
