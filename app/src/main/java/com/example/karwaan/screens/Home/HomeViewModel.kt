package com.example.karwaan.screens.Home

import androidx.lifecycle.viewModelScope
import com.example.karwaan.data.remote.NominatimClient
import com.example.karwaan.data.remote.RouteClient
import com.example.karwaan.data.remote.supabase.SupabaseProvider
import com.example.karwaan.data.remote.supabase.TripRepository
import com.example.karwaan.utils.SearchResult
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import android.provider.Settings
import com.example.karwaan.data.model.Member
import com.example.karwaan.data.remote.supabase.RealtimeManager
import com.example.karwaan.utils.UserLocation
import java.util.UUID



class HomeViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val tripRepository = TripRepository()


    private var suggestionJob: Job? = null
    private var startSuggestionJob: Job? = null
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    private val repo = TripRepository()
    private val realtime = RealtimeManager()
    private var realtimeJob: Job? = null

    private var lastSentLocation: UserLocation? = null


    private val deviceId: String =
        UUID.nameUUIDFromBytes(
            Settings.Secure.getString(
                getApplication<Application>().contentResolver,
                Settings.Secure.ANDROID_ID
            ).toByteArray()
        ).toString()



    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.OnGroupTripClicked -> {
                _uiState.update {
                    it.copy(isGroupTripDialogVisible = true)
                }
            }

            HomeEvent.OnGroupTripDismissed -> {
                _uiState.update {
                    it.copy(isGroupTripDialogVisible = false)
                }
            }

            is HomeEvent.OnDisplayNameChanged -> {
                _uiState.update { it.copy(displayName = event.name) }
            }

            is HomeEvent.OnTripCodeChanged -> {
                _uiState.update { it.copy(tripCode = event.code) }
            }

            HomeEvent.OnCreateTrip -> {
                viewModelScope.launch {

                    try {
                        val trip = repo.createTrip(
                            hostId = deviceId,
                            displayName = _uiState.value.displayName.ifBlank { "You" }
                        )

                        subscribeRealtime(trip.id)

                        _uiState.update {
                            it.copy(
                                isGroupTripDialogVisible = false,
                                isInGroupTrip = true,
                                tripCode = trip.trip_code.toString(),
                                tripId = trip.id,
                                userId = deviceId
                            )
                        }
                        _uiState.value.userLocation?.let { loc ->
                            viewModelScope.launch {
                                tripRepository.updateLocation(
                                    tripId = trip.id,
                                    userId = deviceId,
                                    lat = loc.latitude,
                                    lng = loc.longitude
                                )
                                lastSentLocation = loc
                            }
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }




            HomeEvent.OnJoinTrip -> {
                viewModelScope.launch {
                    try {
                        val code = _uiState.value.tripCode?.toIntOrNull() ?: return@launch

                        val trip = repo.joinTrip(
                            tripCode = code,
                            userId = deviceId,
                            displayName = _uiState.value.displayName.ifBlank { "You" }
                        )

                        subscribeRealtime(trip.id)

                        _uiState.update {
                            it.copy(
                                isGroupTripDialogVisible = false,
                                isInGroupTrip = true,
                                tripId = trip.id,
                                userId = deviceId
                            )
                        }
                        _uiState.value.userLocation?.let { loc ->
                            viewModelScope.launch {
                                tripRepository.updateLocation(
                                    tripId = trip.id,
                                    userId = deviceId,
                                    lat = loc.latitude,
                                    lng = loc.longitude
                                )
                                lastSentLocation = loc
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }




            HomeEvent.OnLeaveTrip -> {
                viewModelScope.launch {
                    val state = _uiState.value
                    try {
                        if (state.tripId != null && state.userId != null) {
                            repo.leaveTrip(state.tripId, state.userId)
                        }
                    } catch (e: Exception) { e.printStackTrace() }

                    realtimeJob?.cancel()
                    _uiState.value = HomeUiState()
                }
            }


            is HomeEvent.OnUserLocationUpdated -> {
                val location = event.location

                // Always update UI (blue dot, camera logic, etc.)
                _uiState.update {
                    it.copy(userLocation = location)
                }

                val state = _uiState.value

                // Only sync to Supabase if user is in an active group trip
                if (!state.isInGroupTrip || state.tripId == null) return

                // 🔥 Distance-based throttling
                val last = lastSentLocation
                if (last != null) {
                    val results = FloatArray(1)
                    android.location.Location.distanceBetween(
                        last.latitude,
                        last.longitude,
                        location.latitude,
                        location.longitude,
                        results
                    )

                    // Ignore very small movements (< 2 meters)
                    if (results[0] < 2f) return
                }

                lastSentLocation = location

                // Push update to Supabase
                viewModelScope.launch {
                    try {
                        tripRepository.updateLocation(
                            tripId = state.tripId,
                            userId = deviceId,
                            lat = location.latitude,
                            lng = location.longitude
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }





            is HomeEvent.OnLocationPermissionResult -> {
                _uiState.update {
                    it.copy(hasLocationPermission = event.granted)
                }
            }

            HomeEvent.OnRecenterRequested -> {
                _uiState.update {
                    it.copy(
                        recenterRequestId = it.recenterRequestId + 1,
                    )
                }
            }

            HomeEvent.OnSearchActivated -> {
                _uiState.update {
                    it.copy(isSearching = true)
                }
            }

            is HomeEvent.OnSearchQueryChanged -> {
                _uiState.update {
                    it.copy(
                        searchQuery = event.query,
                        isSearchLoading = event.query.length >= 2
                    )
                }

                suggestionJob?.cancel()
                suggestionJob = viewModelScope.launch {
                    delay(300) // 🔥 debounce

                    if (event.query.length < 2) {
                        _uiState.update { it.copy(searchSuggestions = emptyList()) }
                        return@launch
                    }

                    fetchSuggestions(event.query)
                }
            }

            is HomeEvent.OnSearchSuggestionSelected -> {
                _uiState.update {
                    it.copy(
                        searchedLocation = event.result,
                        searchQuery = "",
                        searchSuggestions = emptyList(),
                        isSearching = false
                    )
                }
            }



            HomeEvent.OnSearchSubmitted -> {
                resolveLocation(_uiState.value.searchQuery)
            }

            is HomeEvent.OnLocationResolved -> {
                _uiState.update {
                    it.copy(
                        searchedLocation = event.result,
                        isSearching = false
                    )
                }
            }

            HomeEvent.OnClearSearch -> {
                _uiState.update {
                    it.copy(
                        // 🔍 Search reset
                        searchQuery = "",
                        isSearching = false,

                        // 📍 Remove destination
                        searchedLocation = null,

                        // 🧭 Exit directions
                        isDirectionsMode = false,
                        startLocationQuery = "",
                        startSuggestions = emptyList(),
                        directionsError = null,

                        // 🛣 CLEAR ROUTE (THIS REMOVES POLYLINE)
                        routePoints = emptyList(),
                        routeDistanceMeters = null,
                        routeDurationSeconds = null,
                        routeStart = null,
                        routeEnd = null,

                        // 🎯 FORCE RECENTER TO USER
                        recenterRequestId = it.recenterRequestId + 1
                    )
                }
            }


            HomeEvent.OnDirectionsClicked -> {
                _uiState.update {
                    it.copy(isDirectionsMode = true)
                }
            }

            HomeEvent.OnDirectionsDismissed -> {
                _uiState.update {
                    it.copy(
                        isDirectionsMode = false,
                        startLocationQuery = ""
                    )
                }
            }

            is HomeEvent.OnStartLocationChanged -> {
                _uiState.update {
                    it.copy(
                        startLocationQuery = event.query,
                        isStartSearchLoading = event.query.length >= 2
                    )
                }

                startSuggestionJob?.cancel()
                startSuggestionJob = viewModelScope.launch {
                    delay(300)

                    if (event.query.length < 2) {
                        _uiState.update { it.copy(startSuggestions = emptyList()) }
                        return@launch
                    }

                    fetchStartSuggestions(event.query)
                }
            }

            is HomeEvent.OnStartSuggestionSelected -> {
                _uiState.update {
                    it.copy(
                        startLocationQuery = event.result.name,
                        startSuggestions = emptyList()
                    )
                }
            }


            HomeEvent.OnStartFromCurrentLocation -> {
                val user = _uiState.value.userLocation
                val dest = _uiState.value.searchedLocation

                if (user == null) {
                    _uiState.update {
                        it.copy(directionsError = "Waiting for GPS location…")
                    }
                    return
                }

                if (dest == null) {
                    _uiState.update {
                        it.copy(directionsError = "Destination not selected")
                    }
                    return
                }

                val start = SearchResult(
                    name = "Current location",
                    latitude = user.latitude,
                    longitude = user.longitude
                )

                fetchRoute(start, dest)
                _uiState.update { it.copy(isDirectionsMode = false) }
            }




            HomeEvent.OnStartLocationSearch -> {
                val startQuery = _uiState.value.startLocationQuery
                val dest = _uiState.value.searchedLocation

                if (startQuery.isBlank() || dest == null) return

                viewModelScope.launch {
                    try {
                        val results = NominatimClient.api.searchLocation(startQuery)
                        if (results.isNotEmpty()) {
                            val r = results.first()

                            val start = SearchResult(
                                name = r.displayName,
                                latitude = r.latitude.toDouble(),
                                longitude = r.longitude.toDouble()
                            )

                            fetchRoute(start, dest)
                            _uiState.update { it.copy(isDirectionsMode = false) }

                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }


    private fun fetchRoute(start: SearchResult, end: SearchResult) {
        viewModelScope.launch {
            try {
                val coords =
                    "${start.longitude},${start.latitude};${end.longitude},${end.latitude}"

                val response = RouteClient.api.getRoute(coords)

                val points = response.routes.first().geometry.coordinates.map {
                    it[1] to it[0] // lat to lon
                }

                val route = response.routes.first()

                _uiState.update {
                    val correctedDurationSeconds =
                        maxOf(
                            route.duration,
                            (route.distance / 55_000.0) * 3600  // 55 km/h realistic avg
                        )

                    it.copy(

                        routePoints = points,
                        routeDistanceMeters = route.distance,
                        routeDurationSeconds = correctedDurationSeconds
                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private suspend fun fetchStartSuggestions(query: String) {
        try {
            val results = NominatimClient.api.searchLocation(query)

            val suggestions = results.map {
                SearchResult(
                    name = it.displayName,
                    latitude = it.latitude.toDouble(),
                    longitude = it.longitude.toDouble()
                )
            }

            _uiState.update {
                it.copy(
                    startSuggestions = suggestions,
                    isStartSearchLoading = false
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    startSuggestions = emptyList(),
                    isStartSearchLoading = false
                )
            }
        }
    }



    private suspend fun fetchSuggestions(query: String) {
        try {
            val results = NominatimClient.api.searchLocation(query)

            val suggestions = results.map {
                SearchResult(
                    name = it.displayName,
                    latitude = it.latitude.toDouble(),
                    longitude = it.longitude.toDouble()
                )
            }

            _uiState.update {
                it.copy(
                    searchSuggestions = suggestions,
                    isSearchLoading = false
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    searchSuggestions = emptyList(),
                    isSearchLoading = false
                )
            }
        }
    }


    private fun resolveLocation(query: String) {
        if (query.isBlank()) return

        viewModelScope.launch {
            try {
                val results = NominatimClient.api.searchLocation(query)
                if (results.isNotEmpty()) {
                    val result = results.first()
                    onEvent(
                        HomeEvent.OnLocationResolved(
                            SearchResult(
                                name = result.displayName,
                                latitude = result.latitude.toDouble(),
                                longitude = result.longitude.toDouble()
                            )
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun formatDistance(meters: Double): String {
        return if (meters >= 1000)
            String.format("%.1f km", meters / 1000)
        else
            "${meters.toInt()} m"
    }

    fun formatDuration(seconds: Double): String {
        val minutes = (seconds / 60).toInt()
        val hours = minutes / 60
        val mins = minutes % 60

        return if (hours > 0)
            "${hours}h ${mins}m"
        else
            "${mins} min"
    }
    private fun subscribeRealtime(tripId: String) {
        realtimeJob?.cancel()

        realtimeJob = viewModelScope.launch {
            realtime.observeMembers(tripId).collect {
                try {
                    val members =
                        SupabaseProvider.client
                            .from("members")
                            .select {
                                filter {
                                    eq("trip_id", tripId)
                                }
                            }
                            .decodeList<com.example.karwaan.data.model.Member>()

                    _uiState.update {
                        it.copy(tripMembers = members)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }


}