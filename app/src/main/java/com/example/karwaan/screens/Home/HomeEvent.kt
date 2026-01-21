package com.example.karwaan.screens.Home

import com.example.karwaan.utils.UserLocation

sealed class HomeEvent {

    object OnGpsRecenterClicked : HomeEvent()


    data class OnUserLocationUpdated(
        val location: com.example.karwaan.utils.UserLocation
    ) : HomeEvent()


    object OnSearchActivated : HomeEvent()
    data class OnSearchQueryChanged(val query: String) : HomeEvent()
    object OnSearchSubmitted : HomeEvent()

    data class OnLocationResolved(val result: com.example.karwaan.utils.SearchResult) : HomeEvent()
    object OnClearSearch : HomeEvent()

    object OnDirectionsClicked : HomeEvent()
    object OnDirectionsDismissed : HomeEvent()

    data class OnStartLocationChanged(val query: String) : HomeEvent()
    object OnStartFromCurrentLocation : HomeEvent()
    object OnStartLocationSearch : HomeEvent()

    data class OnLocationPermissionResult(val granted: Boolean) : HomeEvent()
}
