package com.example.karwaan.screens.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.karwaan.screens.Home.HomeEvent
import com.example.karwaan.screens.Home.HomeViewModel

@Composable
fun BottomActionButtons(
    modifier: Modifier = Modifier,
    onRecenterClick: () -> Unit,
    onGroupTripClick: () -> Unit = {}
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        FloatingActionButton(
            onClick = onRecenterClick,
            shape = CircleShape
        ) {
            Icon(Icons.Default.GpsFixed, contentDescription = "Recenter")
        }

        FloatingActionButton(
            onClick = onGroupTripClick,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Group, contentDescription = "Group Trip")
        }
    }
}
