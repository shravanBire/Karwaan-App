package com.example.karwaan.screens.Home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.karwaan.utils.SearchResult

@Composable
fun DirectionsDialog(
    startLocationQuery: String,
    startSuggestions: List<SearchResult>,
    onQueryChange: (String) -> Unit,
    onSuggestionSelected: (SearchResult) -> Unit,
    onFromCurrentLocation: () -> Unit,
    onSearchStartLocation: () -> Unit,
    onDismiss: () -> Unit
)
 {
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

                if (startSuggestions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Card {
                        Column {
                            startSuggestions.forEach { suggestion ->
                                ListItem(
                                    headlineContent = { Text(suggestion.name) },
                                    modifier = Modifier.clickable {
                                        onSuggestionSelected(suggestion)
                                    }
                                )
                                Divider()
                            }
                        }
                    }
                }


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
