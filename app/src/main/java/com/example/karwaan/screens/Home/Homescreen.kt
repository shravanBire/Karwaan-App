package com.example.karwaan.screens.Home

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.karwaan.map.MapViewContainer
import com.example.karwaan.screens.components.BottomActionButtons
import com.example.karwaan.screens.components.TopSearchBar
import com.example.karwaan.utils.LocationPermissionHelper
import com.example.karwaan.utils.UserLocation
import com.google.android.gms.location.LocationServices

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel()
) {
    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            viewModel.onEvent(
                HomeEvent.OnLocationPermissionResult(granted)
            )
        }

    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val fusedClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    LaunchedEffect(state.hasLocationPermission) {
        if (
            state.hasLocationPermission &&
            LocationPermissionHelper.hasLocationPermission(context)
        ) {
            fusedClient.getLastLocation()
                .addOnSuccessListener { location ->
                    location?.let {
                        viewModel.onEvent(
                            HomeEvent.OnUserLocationUpdated(
                                UserLocation(it.latitude, it.longitude)
                            )
                        )
                    }
                }
        }
    }




    Box(modifier = Modifier.fillMaxSize()) {

        // 🗺 MAP
        MapViewContainer(
            modifier = Modifier.fillMaxSize(),
            searchedLocation = state.searchedLocation,
            userLocation = state.userLocation
        )

        // 🔍 TOP SEARCH BAR (ONLY ONE)
        TopSearchBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp),

            query = state.searchQuery,
            placeholder = state.searchedLocation?.name ?: "Search destination",
            isEditable = state.isSearching,

            onActivate = {
                viewModel.onEvent(HomeEvent.OnSearchActivated)
            },

            onQueryChange = {
                viewModel.onEvent(HomeEvent.OnSearchQueryChanged(it))
            },

            onSearch = {
                if (state.searchQuery.isNotBlank()) {
                    viewModel.onEvent(HomeEvent.OnSearchSubmitted)
                }
            },

            onClear = {
                viewModel.onEvent(HomeEvent.OnClearSearch)
            }
        )

        // 🔘 BOTTOM UI
        if (state.searchedLocation == null) {

            // Phase A & B
            BottomActionButtons(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp),

                onRecenterClick = {
                    if (LocationPermissionHelper.hasLocationPermission(context)) {
                        viewModel.onEvent(
                            HomeEvent.OnLocationPermissionResult(true)
                        )
                    } else {
                        permissionLauncher.launch(
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    }
                }
            )


        } else {

            // Phase C — Directions button
            Button(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                onClick = {
                    viewModel.onEvent(HomeEvent.OnDirectionsClicked)
                }
            ) {
                Text("Directions")
            }
        }

        // 📍 PHASE D — Directions dialog
        if (state.isDirectionsMode) {
            DirectionsDialog(
                startLocationQuery = state.startLocationQuery,
                onQueryChange = {
                    viewModel.onEvent(HomeEvent.OnStartLocationChanged(it))
                },
                onFromCurrentLocation = {
                    viewModel.onEvent(HomeEvent.OnStartFromCurrentLocation)
                },
                onSearchStartLocation = {
                    viewModel.onEvent(HomeEvent.OnStartLocationSearch)
                },
                onDismiss = {
                    viewModel.onEvent(HomeEvent.OnDirectionsDismissed)
                }
            )
        }
    }
}
