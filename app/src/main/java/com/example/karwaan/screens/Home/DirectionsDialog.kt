package com.example.karwaan.screens.Home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DirectionsDialog(
    startLocationQuery: String,
    onQueryChange: (String) -> Unit,
    onFromCurrentLocation: () -> Unit,
    onSearchStartLocation: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Directions") },
        text = {
            Column {
                OutlinedTextField(
                    value = startLocationQuery,
                    onValueChange = onQueryChange,
                    placeholder = { Text("Start location") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onFromCurrentLocation,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("From current location")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onSearchStartLocation,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Search start location")
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}
