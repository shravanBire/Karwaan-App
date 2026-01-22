package com.example.karwaan.screens.Home

import androidx.lifecycle.ViewModel
import com.example.karwaan.utils.SearchResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import androidx.lifecycle.viewModelScope
import com.example.karwaan.data.remote.NominatimClient
import kotlinx.coroutines.launch


class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    fun onEvent(event: HomeEvent) {
        when (event) {

            is HomeEvent.OnRecenterRequested -> {
                _uiState.update {
                    it.copy(recenterRequestId = it.recenterRequestId + 1)
                }
            }

            HomeEvent.OnGpsRecenterClicked -> {
                // UI will request permission if needed
            }

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

            HomeEvent.OnSearchActivated -> {
                _uiState.update {
                    it.copy(isSearching = true, searchQuery = "")
                }
            }

            is HomeEvent.OnSearchQueryChanged -> {
                _uiState.update {
                    it.copy(searchQuery = event.query)
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

            HomeEvent.OnDirectionsClicked -> {
                _uiState.update {
                    it.copy(isDirectionsMode = true)
                }
            }

            HomeEvent.OnDirectionsDismissed,
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
                    it.copy(startLocationQuery = event.query)
                }
            }

            HomeEvent.OnClearSearch -> {
                _uiState.update {
                    HomeUiState()
                }
            }


            HomeEvent.OnStartFromCurrentLocation -> {
                // TODO: route from current location
            }

            HomeEvent.OnStartLocationSearch -> {
                // TODO: route from custom start location
            }

            is HomeEvent.OnLocationPermissionResult -> {
                // For now, just ignore or store later
                // _uiState.update { it.copy(hasLocationPermission = event.granted) }
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

                    val searchResult = SearchResult(
                        name = result.displayName,
                        latitude = result.latitude.toDouble(),
                        longitude = result.longitude.toDouble()
                    )

                    onEvent(HomeEvent.OnLocationResolved(searchResult))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // later: show error UI
            }
        }
    }


}
