package com.example.karwaan.screens.Home

import com.example.karwaan.utils.SearchResult
import com.example.karwaan.utils.UserLocation

data class HomeUiState(
    val searchQuery: String = "",
    val searchedLocation: SearchResult? = null,

    val isSearching: Boolean = false,
    val isDirectionsMode: Boolean = false,

    val startLocationQuery: String = "",

    val hasLocationPermission: Boolean = false,
    val userLocation: UserLocation? = null,

    val recenterRequestId: Int = 0,

    val searchSuggestions: List<SearchResult> = emptyList(),
    val isSearchLoading: Boolean = false,

    val startSuggestions: List<SearchResult> = emptyList(),
    val isStartSearchLoading: Boolean = false,

    val routeStart: SearchResult? = null,
    val routeEnd: SearchResult? = null,

    val routePoints: List<Pair<Double, Double>> = emptyList(),

    val directionsError: String? = null,

    val routeDistanceMeters: Double? = null,
    val routeDurationSeconds: Double? = null


)

