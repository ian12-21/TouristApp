package com.touristapp.ui.screens.reviews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.touristapp.data.model.Guest
import com.touristapp.data.model.Review
import com.touristapp.data.model.Stay
import com.touristapp.data.repository.TouristRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewsSlide(
    apartmentId: String,
    currentStay: Stay?,
    guests: List<Guest>,
    repository: TouristRepository
) {
    val coroutineScope = rememberCoroutineScope()
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showCreateSheet by remember { mutableStateOf(false) }
    var editingReview by remember { mutableStateOf<Review?>(null) }
    var selectedGuestForEdit by remember { mutableStateOf<Guest?>(null) }

    val currentGuestIds = currentStay?.guestIds ?: emptyList()

    fun refreshReviews() {
        coroutineScope.launch {
            isLoading = true
            reviews = repository.getReviewsForApartment(apartmentId)
            isLoading = false
        }
    }

    LaunchedEffect(apartmentId) {
        refreshReviews()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        } else if (reviews.isEmpty()) {
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
                    text = "No reviews yet",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Be the first to share your experience!",
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
                    AggregateScoreCard(reviews)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${reviews.size} review${if (reviews.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Individual reviews
                items(reviews) { review ->
                    ReviewCard(
                        review = review,
                        isEditable = currentGuestIds.contains(review.guestId),
                        onEdit = {
                            editingReview = review
                            selectedGuestForEdit = guests.find { it.id == review.guestId }
                            showCreateSheet = true
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        // Add Review FAB
        if (currentStay != null && guests.isNotEmpty()) {
            FloatingActionButton(
                onClick = {
                    editingReview = null
                    selectedGuestForEdit = null
                    showCreateSheet = true
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Review")
            }
        }
    }

    if (showCreateSheet) {
        CreateReviewSheet(
            apartmentId = apartmentId,
            currentStay = currentStay,
            guests = guests,
            existingReview = editingReview,
            preselectedGuest = selectedGuestForEdit,
            repository = repository,
            onDismiss = {
                showCreateSheet = false
                editingReview = null
                selectedGuestForEdit = null
            },
            onSaved = {
                showCreateSheet = false
                editingReview = null
                selectedGuestForEdit = null
                refreshReviews()
            }
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
                    text = "/ 10",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val categories = listOf(
                "Cleanliness" to avgCleanliness,
                "Location" to avgLocation,
                "Comfort" to avgComfort,
                "Value for Money" to avgValue,
                "Facilities" to avgFacilities,
                "Communication" to avgCommunication,
                "Wi-Fi" to avgWifi
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
                    .fillMaxWidth(fraction = (score / 10.0).toFloat())
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primary)
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = String.format("%.1f", review.overallScore),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    if (isEditable) {
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = onEdit) {
                            Text("Edit", style = MaterialTheme.typography.labelLarge)
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
        }
    }
}
