package com.example.karwaan.screens.Home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.karwaan.data.remote.supabase.Member

@Composable
fun TripInfoCard(
    tripCode: String,
    members: List<Member>,
    onLeave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(Modifier.padding(12.dp).heightIn(max = 180.dp)) {
            Text("Trip Code: $tripCode", style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(8.dp))

            members.forEach { member ->
                Text(
                    text = "• ${member.display_name}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(8.dp))

            TextButton(onClick = onLeave) {
                Text("Leave Trip", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}