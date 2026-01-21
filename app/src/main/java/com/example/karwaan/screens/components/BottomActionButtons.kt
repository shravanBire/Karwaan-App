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

@Composable
fun BottomActionButtons(
    modifier: Modifier = Modifier,
    onRecenterClick: () -> Unit = {},
    onDirectionsClick: () -> Unit = {},
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
            Icon(
                imageVector = Icons.Default.GpsFixed,
                contentDescription = "Recenter"
            )
        }

        FloatingActionButton(
            onClick = onDirectionsClick,
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.Navigation,
                contentDescription = "Directions"
            )
        }

        FloatingActionButton(
            onClick = onGroupTripClick,
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.Group,
                contentDescription = "Group Trip"
            )
        }
    }
}