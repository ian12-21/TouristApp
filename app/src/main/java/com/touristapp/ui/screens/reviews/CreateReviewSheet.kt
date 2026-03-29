package com.touristapp.ui.screens.reviews

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.touristapp.data.model.Guest
import com.touristapp.data.model.Review
import com.touristapp.data.model.Stay
import com.touristapp.data.repository.TouristRepository
import kotlinx.coroutines.launch

@Composable
fun CreateReviewSheet(
    apartmentId: String,
    currentStay: Stay?,
    guests: List<Guest>,
    existingReview: Review?,
    preselectedGuest: Guest?,
    repository: TouristRepository,
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val isEditMode = existingReview != null

    // State
    var selectedGuest by remember { mutableStateOf(preselectedGuest) }
    var checkingExisting by remember { mutableStateOf(false) }
    var foundExistingReview by remember { mutableStateOf(existingReview) }

    var cleanliness by remember { mutableIntStateOf(existingReview?.cleanliness ?: 5) }
    var location by remember { mutableIntStateOf(existingReview?.location ?: 5) }
    var comfort by remember { mutableIntStateOf(existingReview?.comfort ?: 5) }
    var valueForMoney by remember { mutableIntStateOf(existingReview?.valueForMoney ?: 5) }
    var facilities by remember { mutableIntStateOf(existingReview?.facilities ?: 5) }
    var communication by remember { mutableIntStateOf(existingReview?.communication ?: 5) }
    var wifi by remember { mutableIntStateOf(existingReview?.wifi ?: 5) }
    var comment by remember { mutableStateOf(existingReview?.comment ?: "") }
    var isSaving by remember { mutableStateOf(false) }

    val overallScore = listOf(cleanliness, location, comfort, valueForMoney, facilities, communication, wifi)
        .average()
        .let { Math.round(it * 10) / 10.0 }

    // Auto-select if only one guest
    LaunchedEffect(guests) {
        if (guests.size == 1 && selectedGuest == null) {
            selectedGuest = guests.first()
        }
    }

    // Check for existing review when guest is selected
    LaunchedEffect(selectedGuest) {
        val guest = selectedGuest ?: return@LaunchedEffect
        if (isEditMode) return@LaunchedEffect
        val stayId = currentStay?.id ?: return@LaunchedEffect

        checkingExisting = true
        val existing = repository.getReviewForGuestAndStay(guest.id, stayId)
        if (existing != null) {
            foundExistingReview = existing
            cleanliness = existing.cleanliness
            location = existing.location
            comfort = existing.comfort
            valueForMoney = existing.valueForMoney
            facilities = existing.facilities
            communication = existing.communication
            wifi = existing.wifi
            comment = existing.comment
        } else {
            foundExistingReview = null
            cleanliness = 5; location = 5; comfort = 5
            valueForMoney = 5; facilities = 5; communication = 5; wifi = 5
            comment = ""
        }
        checkingExisting = false
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.95f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (foundExistingReview != null || isEditMode) "Edit Review" else "Add Review",
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 26.sp)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Guest selection (skip if editing or single guest)
                    if (!isEditMode && guests.size > 1) {
                        Text(
                            text = "Who is reviewing?",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            guests.forEach { guest ->
                                val isSelected = selectedGuest?.id == guest.id
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { selectedGuest = guest }
                                        .then(
                                            if (isSelected) Modifier.border(
                                                1.dp,
                                                MaterialTheme.colorScheme.primary,
                                                RoundedCornerShape(14.dp)
                                            ) else Modifier
                                        ),
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected)
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = if (isSelected)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = guest.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (isSelected)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    if (selectedGuest != null && !checkingExisting) {
                        if (foundExistingReview != null && !isEditMode) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = "${selectedGuest!!.name} already has a review for this stay. You can edit it below.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Rating categories
                        Text(
                            text = "Rate your experience",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        RatingSlider("Cleanliness", cleanliness) { cleanliness = it }
                        RatingSlider("Location", location) { location = it }
                        RatingSlider("Comfort", comfort) { comfort = it }
                        RatingSlider("Value for money", valueForMoney) { valueForMoney = it }
                        RatingSlider("Facilities", facilities) { facilities = it }
                        RatingSlider("Communication", communication) { communication = it }
                        RatingSlider("Wi-Fi", wifi) { wifi = it }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Overall score display
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Overall: ",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = String.format("%.1f", overallScore),
                                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = " / 10",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Comment
                        Text(
                            text = "Comment (optional)",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = comment,
                            onValueChange = { comment = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp),
                            placeholder = {
                                Text(
                                    "Share your experience...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                    } else if (selectedGuest == null) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Select a guest to continue",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                // Submit button
                if (selectedGuest != null && !checkingExisting) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            isSaving = true
                            coroutineScope.launch {
                                try {
                                    val review = Review(
                                        apartmentId = apartmentId,
                                        stayId = currentStay?.id ?: "",
                                        guestId = selectedGuest!!.id,
                                        guestName = selectedGuest!!.name,
                                        cleanliness = cleanliness,
                                        location = location,
                                        comfort = comfort,
                                        valueForMoney = valueForMoney,
                                        facilities = facilities,
                                        communication = communication,
                                        wifi = wifi,
                                        overallScore = overallScore,
                                        comment = comment.trim()
                                    )

                                    val existingId = foundExistingReview?.id ?: existingReview?.id
                                    val success = if (existingId != null) {
                                        repository.updateReview(existingId, review)
                                    } else {
                                        repository.createReview(review)
                                    }

                                    isSaving = false
                                    if (success) onSaved()
                                } catch (e: Exception) {
                                    isSaving = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = !isSaving,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (foundExistingReview != null || isEditMode) "Update Review" else "Submit Review",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingSlider(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "$value",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 1f..10f,
            steps = 8,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        )
    }
}
