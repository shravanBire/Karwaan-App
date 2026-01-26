package com.example.karwaan.screens.Home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GroupTripDialog(
    displayName: String,
    tripCode: String?,
    onNameChange: (String) -> Unit,
    onCodeChange: (String) -> Unit,
    onCreate: () -> Unit,
    onJoin: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Group Trip") },
        text = {
            Column {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selectedTab == 0, onClick = { selectedTab = 0 }) {
                        Text("Create")
                    }
                    Tab(selectedTab == 1, onClick = { selectedTab = 1 }) {
                        Text("Join")
                    }
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = displayName,
                    onValueChange = onNameChange,
                    label = { Text("Your name") },
                    singleLine = true
                )

                if (selectedTab == 1) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tripCode.orEmpty(),
                        onValueChange = onCodeChange,
                        label = { Text("Trip code") },
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = if (selectedTab == 0) onCreate else onJoin,
                enabled = displayName.isNotBlank()
            ) {
                Text(if (selectedTab == 0) "Create Trip" else "Join Trip")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}