package com.example.karwaan.screens.Home

import android.annotation.SuppressLint
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
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

import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority


@SuppressLint("MissingPermission")
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


    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return

                viewModel.onEvent(
                    HomeEvent.OnUserLocationUpdated(
                        UserLocation(
                            location.latitude,
                            location.longitude
                        )
                    )
                )
            }
        }
    }


    val locationRequest = remember {
        LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2000L // update every 2 seconds
        )
            .setMinUpdateDistanceMeters(3f) // move 3 meters
            .build()
    }

    LaunchedEffect(state.hasLocationPermission) {
        if (
            state.hasLocationPermission &&
            LocationPermissionHelper.hasLocationPermission(context)
        ) {
            fusedClient.removeLocationUpdates(locationCallback)

            runCatching {
                fusedClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }.onFailure {
                it.printStackTrace()
            }
        }
    }


    DisposableEffect(Unit) {
        onDispose {
            fusedClient.removeLocationUpdates(locationCallback)
        }
    }



    Box(modifier = Modifier.fillMaxSize()) {

        // 🗺 MAP
        MapViewContainer(
            modifier = Modifier.fillMaxSize(),
            searchedLocation = state.searchedLocation,
            userLocation = state.userLocation,
            routePoints = state.routePoints,
            recenterRequestId = state.recenterRequestId
              )

        if (state.routeDistanceMeters != null && state.routeDurationSeconds != null) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 96.dp),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "${viewModel.formatDuration(state.routeDurationSeconds!!)} • ${
                            viewModel.formatDistance(state.routeDistanceMeters!!)
                        }",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // 🔍 TOP SEARCH BAR (ONLY ONE)
        TopSearchBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 50.dp, start = 16.dp, end = 16.dp),

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

        if (state.isSearching && state.searchSuggestions.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 110.dp)
                    .fillMaxWidth(0.95f),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Column {
                    state.searchSuggestions.forEach { suggestion ->
                        ListItem(
                            headlineContent = { Text(suggestion.name) },
                            modifier = Modifier.clickable {
                                viewModel.onEvent(
                                    HomeEvent.OnSearchSuggestionSelected(suggestion)
                                )
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }


        // 🔘 BOTTOM UI
        if (state.searchedLocation == null) {

            // Phase A & B
            BottomActionButtons(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp),

                onRecenterClick = {
                    if (LocationPermissionHelper.hasLocationPermission(context)) {
                        viewModel.onEvent(HomeEvent.OnLocationPermissionResult(true))
                        viewModel.onEvent(HomeEvent.OnRecenterRequested)
                    } else {
                        permissionLauncher.launch(
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    }
                }
            )


        } else {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // 🔵 RECENTER (same functionality as before)
                FloatingActionButton(
                    onClick = {
                        if (LocationPermissionHelper.hasLocationPermission(context)) {
                            viewModel.onEvent(HomeEvent.OnRecenterRequested)
                        } else {
                            permissionLauncher.launch(
                                android.Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = "Recenter"
                    )
                }

                // 🧭 DIRECTIONS
                Button(
                    onClick = {
                        viewModel.onEvent(HomeEvent.OnDirectionsClicked)
                    }
                ) {
                    Text("Directions")
                }
            }
        }


        // 📍 PHASE D — Directions dialog
        if (state.isDirectionsMode) {
            DirectionsDialog(
                startLocationQuery = state.startLocationQuery,
                startSuggestions = state.startSuggestions,
                onQueryChange = {
                    viewModel.onEvent(HomeEvent.OnStartLocationChanged(it))
                },
                onSuggestionSelected = {
                    viewModel.onEvent(HomeEvent.OnStartSuggestionSelected(it))
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
            state.directionsError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }
        }
    }
}
