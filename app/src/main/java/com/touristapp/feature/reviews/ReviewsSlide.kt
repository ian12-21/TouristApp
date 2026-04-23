package com.touristapp.feature.reviews

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.touristapp.R
import com.touristapp.core.ui.components.ErrorContent
import com.touristapp.core.util.decodeDoodle
import com.touristapp.data.model.Guest
import com.touristapp.data.model.Review
import com.touristapp.data.model.Stay
import java.text.SimpleDateFormat
import java.util.Locale

private fun scoreColor(score: Double): Color = when {
    score <= 3.0 -> Color(0xFFFF6B6B)
    score <= 5.0 -> Color(0xFFFF9F43)
    score <= 7.0 -> Color(0xFFFFD93D)
    score <= 8.5 -> Color(0xFF6BCB77)
    else -> Color(0xFF4ECDC4)
}

private fun qualitativeLabel(score: Double): String = when {
    score >= 9.0 -> "Exceptional"
    score >= 8.0 -> "Excellent"
    score >= 7.0 -> "Very Good"
    score >= 6.0 -> "Good"
    score >= 5.0 -> "Average"
    else -> "Below Average"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewsSlide(
    apartmentId: String,
    currentStay: Stay?,
    guests: List<Guest>
) {
    val viewModel: ReviewsViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val currentGuestIds = currentStay?.guestIds ?: emptyList()

    LaunchedEffect(apartmentId) {
        viewModel.loadReviews(apartmentId)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        } else if (state.error != null) {
            ErrorContent(
                message = state.error,
                onRetry = { viewModel.loadReviews(apartmentId) }
            )
        } else if (state.reviews.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.reviews_empty_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.reviews_empty_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 80.dp)
            ) {
                // Aggregate score header
                item {
                    AggregateScoreCard(state.reviews)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (state.reviews.size != 1) stringResource(R.string.reviews_count_plural, state.reviews.size)
                               else stringResource(R.string.reviews_count, state.reviews.size),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Individual reviews
                items(state.reviews, key = { it.id }) { review ->
                    Column(modifier = Modifier.animateItem()) {
                        ReviewCard(
                            review = review,
                            isEditable = currentGuestIds.contains(review.guestId),
                            onEdit = {
                                viewModel.openEditSheet(review, guests.find { it.id == review.guestId })
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }

        // Add Review FAB
        if (currentStay != null && guests.isNotEmpty()) {
            FloatingActionButton(
                onClick = { viewModel.openCreateSheet() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_add_review))
            }
        }
    }

    if (state.showCreateSheet) {
        CreateReviewSheet(
            apartmentId = apartmentId,
            currentStay = currentStay,
            guests = guests,
            viewModel = viewModel
        )
    }
}

@Composable
private fun AggregateScoreCard(reviews: List<Review>) {
    val avgScore = reviews.map { it.overallScore }.average()
    val avgCleanliness = reviews.map { it.cleanliness }.average()
    val avgLocation = reviews.map { it.location }.average()
    val avgComfort = reviews.map { it.comfort }.average()
    val avgValue = reviews.map { it.valueForMoney }.average()
    val avgFacilities = reviews.map { it.facilities }.average()
    val avgCommunication = reviews.map { it.communication }.average()
    val avgWifi = reviews.map { it.wifi }.average()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = String.format("%.1f", avgScore),
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 48.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.reviews_score_suffix),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            Text(
                text = qualitativeLabel(avgScore),
                style = MaterialTheme.typography.labelLarge,
                color = scoreColor(avgScore)
            )

            Spacer(modifier = Modifier.height(16.dp))

            val categories = listOf(
                stringResource(R.string.rating_cleanliness) to avgCleanliness,
                stringResource(R.string.rating_location) to avgLocation,
                stringResource(R.string.rating_comfort) to avgComfort,
                stringResource(R.string.rating_value_for_money) to avgValue,
                stringResource(R.string.rating_facilities) to avgFacilities,
                stringResource(R.string.rating_communication) to avgCommunication,
                stringResource(R.string.rating_wifi) to avgWifi
            )

            categories.forEach { (name, score) ->
                CategoryBar(name, score)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun CategoryBar(name: String, score: Double) {
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }
    val animatedFraction by animateFloatAsState(
        targetValue = if (appeared) (score / 10.0).toFloat() else 0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
        label = "barFill"
    )
    val barColor = scoreColor(score)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(140.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = animatedFraction)
                    .clip(RoundedCornerShape(4.dp))
                    .background(barColor)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = String.format("%.1f", score),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.width(32.dp)
        )
    }
}

@Composable
private fun ReviewCard(
    review: Review,
    isEditable: Boolean,
    onEdit: () -> Unit
) {
    val badgeColor = scoreColor(review.overallScore)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = review.guestName.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = review.guestName.ifBlank { "Guest" },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        review.createdAt?.let { ts ->
                            Text(
                                text = SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(ts.toDate()),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = badgeColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = String.format("%.1f", review.overallScore),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    if (isEditable) {
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = onEdit) {
                            Text(stringResource(R.string.reviews_edit), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            if (review.comment.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = review.comment,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
                )
            }

            review.doodleBase64?.let { b64 ->
                val bitmap = remember(b64) { decodeDoodle(b64) }
                bitmap?.let {
                    Spacer(modifier = Modifier.height(12.dp))
                    Image(
                        bitmap = it,
                        contentDescription = stringResource(R.string.review_doodle_cd),
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(3f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}
