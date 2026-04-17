package com.touristapp.feature.places

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Nightlife
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsHandball
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.touristapp.R
import com.touristapp.core.ui.components.ErrorContent
import com.touristapp.data.model.Place
import com.touristapp.data.model.getDistanceFor

enum class PlaceCategory(
    val key: String,
    @StringRes val displayNameRes: Int,
    val icon: ImageVector
) {
    BEACH("beach", R.string.category_beach, Icons.Default.BeachAccess),
    RESTAURANT("restaurant", R.string.category_restaurant, Icons.Default.Restaurant),
    CAFE("cafe", R.string.category_cafe, Icons.Default.LocalCafe),
    STORE("store", R.string.category_store, Icons.Default.ShoppingBag),
    PHARMACY("pharmacy", R.string.category_pharmacy, Icons.Default.LocalPharmacy),
    ATTRACTION("attraction", R.string.category_attraction, Icons.Default.Place),
    NIGHTLIFE("nightlife", R.string.category_nightlife, Icons.Default.Nightlife),
    ACTIVITY("activity", R.string.category_activity, Icons.Default.SportsHandball);

    companion object {
        fun fromKey(key: String): PlaceCategory? = entries.find { it.key == key }
    }
}

fun distanceIcon(type: String): ImageVector = when (type) {
    "car" -> Icons.Default.DirectionsCar
    "bus" -> Icons.Default.DirectionsBus
    else -> Icons.Default.DirectionsWalk
}

@Composable
fun PlacesSlide(
    apartmentId: String,
    onSeeAll: (PlaceCategory) -> Unit = {},
    onPlaceClick: (Place) -> Unit = {},
    onPlacesLoaded: (List<Place>) -> Unit = {}
) {
    val viewModel: PlacesViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(apartmentId) {
        viewModel.loadPlaces(apartmentId)
    }

    LaunchedEffect(state.places) {
        if (state.places.isNotEmpty()) {
            onPlacesLoaded(state.places)
        }
    }

    val grouped = state.places.groupBy { PlaceCategory.fromKey(it.category) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 24.dp)
    ) {
        Text(
            text = stringResource(R.string.places_title),
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 34.sp),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.places_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.error != null) {
            ErrorContent(
                message = state.error,
                onRetry = { viewModel.loadPlaces(apartmentId) }
            )
        } else {
            PlaceCategory.entries.forEach { category ->
                val categoryPlaces = grouped[category] ?: emptyList()
                if (categoryPlaces.isNotEmpty()) {
                    CategoryRow(
                        category = category,
                        places = categoryPlaces.take(7),
                        apartmentId = apartmentId,
                        onSeeAll = { onSeeAll(category) },
                        onPlaceClick = onPlaceClick
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            if (state.places.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.places_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryRow(
    category: PlaceCategory,
    places: List<Place>,
    apartmentId: String,
    onSeeAll: () -> Unit,
    onPlaceClick: (Place) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = stringResource(category.displayNameRes),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        TextButton(
            onClick = onSeeAll,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = stringResource(R.string.places_see_all),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(places, key = { it.id }) { place ->
            PlaceCard(
                place = place,
                apartmentId = apartmentId,
                onClick = { onPlaceClick(place) }
            )
        }
    }
}

@Composable
private fun PlaceCard(place: Place, apartmentId: String, onClick: () -> Unit = {}) {
    val cardShape = RoundedCornerShape(16.dp)
    val link = place.getDistanceFor(apartmentId)

    Surface(
        onClick = onClick,
        modifier = Modifier
            .width(160.dp)
            .height(200.dp),
        shape = cardShape,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val imageUrl = place.thumbImageUrl.ifBlank { place.images.firstOrNull() }
            if (imageUrl != null && imageUrl.isNotBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = place.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            if (link != null) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = distanceIcon(link.distanceType),
                            contentDescription = link.distanceType,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = link.distance,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (place.address.isNotBlank()) {
                    Text(
                        text = place.address,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
