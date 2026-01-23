package com.example.karwaan.screens.Home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.karwaan.data.remote.NominatimClient
import com.example.karwaan.data.remote.RouteClient
import com.example.karwaan.utils.SearchResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay


class HomeViewModel : ViewModel() {

    private var suggestionJob: Job? = null
    private var startSuggestionJob: Job? = null
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    fun onEvent(event: HomeEvent) {
        when (event) {

            // 🔵 GPS / LOCATION
            is HomeEvent.OnUserLocationUpdated -> {
                _uiState.update {
                    it.copy(userLocation = event.location)
                }
            }

            is HomeEvent.OnLocationPermissionResult -> {
                _uiState.update {
                    it.copy(hasLocationPermission = event.granted)
                }
            }

            HomeEvent.OnRecenterRequested -> {
                _uiState.update {
                    it.copy(recenterRequestId = it.recenterRequestId + 1)
                }
            }

            // 🔍 SEARCH
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
                        searchQuery = "",
                        searchedLocation = null,
                        isSearching = false,
                        isDirectionsMode = false,
                        startLocationQuery = ""
                    )
                }
            }

            // 🧭 DIRECTIONS
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
                    it.copy(
                        routePoints = points,
                        routeDistanceMeters = route.distance,
                        routeDurationSeconds = route.duration
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
}
