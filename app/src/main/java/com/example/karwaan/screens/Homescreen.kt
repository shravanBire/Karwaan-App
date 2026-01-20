package com.example.karwaan.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.karwaan.map.MapViewContainer
import com.example.karwaan.screens.components.BottomActionButtons
import com.example.karwaan.screens.components.TopSearchBar

@Composable
fun HomeScreen() {
    Box(modifier = Modifier.fillMaxSize()) {

        // Full screen map
        MapViewContainer(
            modifier = Modifier.fillMaxSize()
        )

        // Dummy search bar at top
        TopSearchBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
        )

        // Bottom action buttons
        BottomActionButtons(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }
}