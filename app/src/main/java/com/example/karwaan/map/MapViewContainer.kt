package com.example.karwaan.map

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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
import kotlinx.coroutines.delay
import org.maplibre.android.style.layers.Property.LINE_CAP_ROUND
import org.maplibre.android.style.layers.Property.LINE_JOIN_ROUND
import org.maplibre.geojson.FeatureCollection
import org.maplibre.android.style.expressions.Expression.get
import org.maplibre.android.style.layers.Property.ICON_ANCHOR_CENTER


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

// Generate unique icon IDs for each member color
private fun getMemberIconId(color: String): String = "member-icon-$color"

@SuppressLint("MissingPermission")
@Composable
fun MapViewContainer(
    modifier: Modifier = Modifier,
    searchedLocation: SearchResult?,
    userLocation: UserLocation?,
    routePoints: List<Pair<Double, Double>>,
    recenterRequestId: Int,
    tripMembers: List<com.example.karwaan.data.remote.supabase.Member>,
    currentUserId: String?
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    var animatedRoutePoints by remember { mutableStateOf<List<Pair<Double, Double>>>(emptyList()) }
    var style by remember { mutableStateOf<Style?>(null) }

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
                    ) { loadedStyle ->
                        style = loadedStyle

                        // 🔵 User location blue dot
                        loadedStyle.addImage(
                            USER_LOCATION_ICON_ID,
                            context.getDrawable(R.drawable.user_location_dot)!!
                        )

                        // 🔴 Destination pin
                        loadedStyle.addImage(
                            DESTINATION_ICON_ID,
                            context.getDrawable(
                                org.maplibre.android.R.drawable.maplibre_marker_icon_default
                            )!!
                        )

                        // Create colored markers for members
                        createColoredMarkerIcons(loadedStyle, tripMembers.map { it.marker_color }.distinct())
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

     LaunchedEffect(tripMembers) {
         map?.let { m ->
             updateMembersOnMap(
                 map = m,
                 members = tripMembers,
                 currentUserId = currentUserId
             )
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

private fun createColoredMarkerIcons(style: Style, colors: List<String>) {
    colors.forEach { colorHex ->
        val iconId = getMemberIconId(colorHex)

        // Skip if already added
        if (style.getImage(iconId) != null) return@forEach

        val bitmap = createColoredCircleBitmap(colorHex)
        style.addImage(iconId, bitmap)
    }
}

private fun createColoredCircleBitmap(colorHex: String): Bitmap {
    val size = 48
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = try {
            Color.parseColor(colorHex)
        } catch (e: Exception) {
            Color.BLUE
        }
        style = Paint.Style.FILL
    }

    // Draw circle
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 2f, paint)

    // Add white border
    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 2f, borderPaint)

    return bitmap
}

private fun updateMembersOnMap(
    map: MapLibreMap,
    members: List<com.example.karwaan.data.remote.supabase.Member>,
    currentUserId: String?
) {
    val style = map.style ?: return

    // Filter: only show other active members with valid locations
    val otherMembers = members.filter { member ->
        member.user_id != currentUserId &&
                member.is_active == true &&
                member.latitude != null &&
                member.longitude != null
    }

    Log.d("MembersMap", "Showing ${otherMembers.size} members on map")

    if (otherMembers.isEmpty()) {
        // Clear the source if no members
        style.getSourceAs<GeoJsonSource>(MEMBERS_SOURCE_ID)?.setGeoJson(
            FeatureCollection.fromFeatures(emptyList())
        )
        return
    }

    val features = otherMembers.map { member ->
        Feature.fromGeometry(
            Point.fromLngLat(member.longitude!!, member.latitude!!)
        ).apply {
            addStringProperty("name", member.display_name)
            addStringProperty("color", member.marker_color)
            addStringProperty("iconId", getMemberIconId(member.marker_color))
        }
    }

    val collection = FeatureCollection.fromFeatures(features)
    val source = style.getSourceAs<GeoJsonSource>(MEMBERS_SOURCE_ID)

    if (source == null) {
        // Create new source and layer
        style.addSource(GeoJsonSource(MEMBERS_SOURCE_ID, collection))

        style.addLayerAbove(
            SymbolLayer(MEMBERS_LAYER_ID, MEMBERS_SOURCE_ID).withProperties(
                iconImage(get("iconId")), // Use the color-specific icon
                iconSize(1.0f),
                iconAnchor(ICON_ANCHOR_CENTER),
                iconAllowOverlap(true),
                iconIgnorePlacement(true),
                textField(get("name")),
                textSize(12f),
                textOffset(arrayOf(0f, 1.5f)),
                textColor(Color.BLACK),
                textHaloColor(Color.WHITE),
                textHaloWidth(2f),
                textAllowOverlap(false),
                textIgnorePlacement(false)
            ),
            USER_LOCATION_LAYER_ID
        )
    } else {
        // Update existing source
        source.setGeoJson(collection)
    }

    // Log for debugging
    otherMembers.forEach { member ->
        Log.d(
            "MembersMap",
            "Member: ${member.display_name} at (${member.latitude}, ${member.longitude}) color: ${member.marker_color}"
        )
    }
}
