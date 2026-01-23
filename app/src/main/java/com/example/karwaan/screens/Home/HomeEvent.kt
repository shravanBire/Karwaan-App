package com.example.karwaan.screens.Home

import com.example.karwaan.utils.SearchResult
import com.example.karwaan.utils.UserLocation

sealed class HomeEvent {

    object OnRecenterRequested : HomeEvent()
    data class OnUserLocationUpdated(val location: UserLocation) : HomeEvent()
    data class OnLocationPermissionResult(val granted: Boolean) : HomeEvent()
    object OnSearchActivated : HomeEvent()
    data class OnSearchQueryChanged(val query: String) : HomeEvent()
    object OnSearchSubmitted : HomeEvent()
    data class OnLocationResolved(val result: SearchResult) : HomeEvent()
    object OnClearSearch : HomeEvent()
    object OnDirectionsClicked : HomeEvent()
    object OnDirectionsDismissed : HomeEvent()
    data class OnStartLocationChanged(val query: String) : HomeEvent()
    object OnStartFromCurrentLocation : HomeEvent()
    object OnStartLocationSearch : HomeEvent()
    data class OnSearchSuggestionSelected(val result: SearchResult) : HomeEvent()
}
