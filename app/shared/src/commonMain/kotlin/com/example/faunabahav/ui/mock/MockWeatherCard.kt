package com.example.faunabahav.ui.mock

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.faunabahav.ui.theme.AccentOrange

/** No weather data source is wired up yet — placeholder card, swap for a real feed later. */
@Composable
fun MockWeatherCard(modifier: Modifier = Modifier) {
    Card(modifier) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.WbSunny, contentDescription = null, tint = AccentOrange)
            Column(Modifier.padding(start = 12.dp)) {
                Text("28°C · Sunny", style = MaterialTheme.typography.titleSmall)
                Text("Farm A weather (preview)", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
