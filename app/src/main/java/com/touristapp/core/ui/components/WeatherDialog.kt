package com.touristapp.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.touristapp.data.model.DailyForecast
import com.touristapp.data.model.WeatherInfo
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun WeatherDialog(
    weatherInfo: WeatherInfo,
    weekForecast: List<DailyForecast> = emptyList(),
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column {
                WeatherHero(weatherInfo, onDismiss = onDismiss)

                // ── Forecast ────────────────────────────────────────────────
                if (weekForecast.isNotEmpty()) {
                    val today = LocalDate.now()

                    Text(
                        text = "5-DAY FORECAST",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 24.dp, top = 20.dp, bottom = 4.dp)
                    )

                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        weekForecast.forEach { day ->
                            ForecastRow(
                                forecast = day,
                                isToday = day.date == today
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun WeatherHero(weatherInfo: WeatherInfo, onDismiss: () -> Unit) {
    val gradient = skyGradientFor(weatherInfo.iconCode)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Brush.verticalGradient(gradient))
    ) {
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White.copy(alpha = 0.9f)
            )
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White.copy(alpha = 0.85f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = weatherInfo.cityName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ── Left: icon + temperature + description ──────────────
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = weatherIconFor(weatherInfo.iconCode),
                        contentDescription = weatherInfo.condition,
                        modifier = Modifier.size(56.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "${weatherInfo.tempCelsius.roundToInt()}°",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = weatherInfo.description.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // ── Right: feels like + humidity ────────────────────────
                Column(horizontalAlignment = Alignment.End) {
                    WeatherChip(
                        icon = Icons.Default.Thermostat,
                        label = "Feels like",
                        value = "${weatherInfo.feelsLike.roundToInt()}°C"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    WeatherChip(
                        icon = Icons.Default.WaterDrop,
                        label = "Humidity",
                        value = "${weatherInfo.humidity}%"
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherChip(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .background(Color.White.copy(alpha = 0.18f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = Color.White.copy(alpha = 0.9f)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun ForecastRow(
    forecast: DailyForecast,
    isToday: Boolean
) {
    val dayLabel = if (isToday) "Today"
    else forecast.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .then(
                if (isToday) Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                else Modifier
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = dayLabel,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(48.dp)
        )

        Icon(
            imageVector = weatherIconFor(forecast.iconCode),
            contentDescription = forecast.condition,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.weight(1f))

        // Colored dot encodes the day's high temperature on a cold→hot scale.
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(tempColor(forecast.maxTempCelsius))
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "${forecast.minTempCelsius.roundToInt()}°",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(32.dp)
        )

        Text(
            text = "${forecast.maxTempCelsius.roundToInt()}°",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(32.dp)
        )
    }
}

/**
 * Maps an absolute temperature (°C) to a fixed cold→hot color.
 * ≤0° deep blue, ~10° cyan, ~20° green, ~28° orange, ≥35° red.
 */
private fun tempColor(tempCelsius: Double): Color {
    val stops = listOf(
        0.0 to Color(0xFF2962FF),   // cold — blue
        10.0 to Color(0xFF00B8D4),  // cool — cyan
        20.0 to Color(0xFF43A047),  // mild — green
        28.0 to Color(0xFFFB8C00),  // warm — orange
        35.0 to Color(0xFFE53935)   // hot  — red
    )
    if (tempCelsius <= stops.first().first) return stops.first().second
    if (tempCelsius >= stops.last().first) return stops.last().second
    for (i in 0 until stops.size - 1) {
        val (lowT, lowC) = stops[i]
        val (highT, highC) = stops[i + 1]
        if (tempCelsius in lowT..highT) {
            val fraction = ((tempCelsius - lowT) / (highT - lowT)).toFloat()
            return androidx.compose.ui.graphics.lerp(lowC, highC, fraction)
        }
    }
    return stops.last().second
}

private fun skyGradientFor(iconCode: String): List<Color> {
    val isNight = iconCode.endsWith("n")
    return when {
        iconCode.startsWith("01") && !isNight -> listOf(Color(0xFF2196F3), Color(0xFF64B5F6))
        iconCode.startsWith("01") && isNight -> listOf(Color(0xFF1A237E), Color(0xFF3949AB))
        iconCode.startsWith("02") || iconCode.startsWith("03") || iconCode.startsWith("04") ->
            listOf(Color(0xFF546E7A), Color(0xFF90A4AE))
        iconCode.startsWith("09") || iconCode.startsWith("10") ->
            listOf(Color(0xFF37474F), Color(0xFF607D8B))
        iconCode.startsWith("11") -> listOf(Color(0xFF263238), Color(0xFF455A64))
        iconCode.startsWith("13") -> listOf(Color(0xFF78909C), Color(0xFFB0BEC5))
        iconCode.startsWith("50") -> listOf(Color(0xFF607D8B), Color(0xFF90A4AE))
        else -> listOf(Color(0xFF2196F3), Color(0xFF64B5F6))
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
