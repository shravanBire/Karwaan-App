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
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        FloatingActionButton(
            onClick = { },
            shape = CircleShape
        ) {
            Icon(Icons.Default.GpsFixed, contentDescription = "Recenter")
        }

        FloatingActionButton(
            onClick = { },
            shape = CircleShape
        ) {
            Icon(Icons.Default.Navigation, contentDescription = "Navigate")
        }

        FloatingActionButton(
            onClick = { },
            shape = CircleShape
        ) {
            Icon(Icons.Default.Group, contentDescription = "Group Trip")
        }
    }
}