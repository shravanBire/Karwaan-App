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

    val recenterRequestId: Int = 0
)

