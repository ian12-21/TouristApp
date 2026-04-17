package com.touristapp.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.touristapp.data.model.WeatherInfo
import kotlin.math.roundToInt

@Composable
fun WeatherDialog(
    weatherInfo: WeatherInfo,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = weatherInfo.cityName,
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(16.dp))

                Icon(
                    imageVector = weatherIconFor(weatherInfo.iconCode),
                    contentDescription = weatherInfo.condition,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${weatherInfo.tempCelsius.roundToInt()}°C",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = weatherInfo.description.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    WeatherDetail(
                        label = "Feels like",
                        value = "${weatherInfo.feelsLike.roundToInt()}°C"
                    )
                    WeatherDetail(
                        label = "Humidity",
                        value = "${weatherInfo.humidity}%"
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
private fun WeatherDetail(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

fun weatherIconFor(iconCode: String): ImageVector {
    return when {
        iconCode.startsWith("01") && iconCode.endsWith("d") -> Icons.Default.WbSunny
        iconCode.startsWith("01") && iconCode.endsWith("n") -> Icons.Default.NightsStay
        iconCode.startsWith("02") || iconCode.startsWith("03") || iconCode.startsWith("04") -> Icons.Default.Cloud
        iconCode.startsWith("09") || iconCode.startsWith("10") -> Icons.Default.WaterDrop
        iconCode.startsWith("11") -> Icons.Default.FlashOn
        iconCode.startsWith("13") -> Icons.Default.AcUnit
        iconCode.startsWith("50") -> Icons.Default.Cloud
        else -> Icons.Default.WbSunny
    }
}
