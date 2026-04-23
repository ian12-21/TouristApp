package com.touristapp.feature.reviews

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.touristapp.R
import com.touristapp.core.ui.components.DoodleCanvas
import com.touristapp.core.util.decodeDoodle
import com.touristapp.data.model.Guest
import com.touristapp.data.model.Stay

private fun scoreColor(score: Double): Color = when {
    score <= 3.0 -> Color(0xFFFF6B6B)
    score <= 5.0 -> Color(0xFFFF9F43)
    score <= 7.0 -> Color(0xFFFFD93D)
    score <= 8.5 -> Color(0xFF6BCB77)
    else -> Color(0xFF4ECDC4)
}

@Composable
fun CreateReviewSheet(
    apartmentId: String,
    currentStay: Stay?,
    guests: List<Guest>,
    viewModel: ReviewsViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Auto-select if only one guest
    LaunchedEffect(guests) {
        if (guests.size == 1 && state.selectedGuest == null) {
            viewModel.selectGuest(guests.first(), currentStay?.id)
        }
    }

    Dialog(
        onDismissRequest = { viewModel.dismissCreateSheet() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                ) { viewModel.dismissCreateSheet() },
            contentAlignment = Alignment.Center
        ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.90f)
                .fillMaxHeight(0.90f)
                .clickable(
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                ) { /* consume clicks so they don't dismiss */ }
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = Color.Black.copy(alpha = 0.3f),
                    spotColor = Color.Black.copy(alpha = 0.3f)
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(24.dp)
                ),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
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
                        text = stringResource(if (state.foundExistingReview != null || state.isEditMode) R.string.review_edit_title else R.string.review_create_title),
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 26.sp)
                    )
                    IconButton(onClick = { viewModel.dismissCreateSheet() }) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cd_close))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .animateContentSize()
                ) {
                    // Guest selection (skip if editing or single guest)
                    if (!state.isEditMode && guests.size > 1) {
                        Text(
                            text = stringResource(R.string.review_who_reviewing),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            guests.forEach { guest ->
                                val isSelected = state.selectedGuest?.id == guest.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.selectGuest(guest, currentStay?.id) },
                                    label = { Text(guest.name) },
                                    leadingIcon = {
                                        Icon(
                                            if (isSelected) Icons.Default.Check else Icons.Default.Person,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        selectedLabelColor = MaterialTheme.colorScheme.primary,
                                        selectedLeadingIconColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    if (state.selectedGuest != null && !state.isCheckingExisting) {
                        if (state.foundExistingReview != null && !state.isEditMode) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = stringResource(R.string.review_existing_notice, state.selectedGuest!!.name),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Rating categories
                        Text(
                            text = stringResource(R.string.review_rate_experience),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        RatingSlider(stringResource(R.string.rating_cleanliness), state.cleanliness) { viewModel.updateRating("cleanliness", it) }
                        RatingSlider(stringResource(R.string.rating_location), state.location) { viewModel.updateRating("location", it) }
                        RatingSlider(stringResource(R.string.rating_comfort), state.comfort) { viewModel.updateRating("comfort", it) }
                        RatingSlider(stringResource(R.string.rating_value_for_money), state.valueForMoney) { viewModel.updateRating("valueForMoney", it) }
                        RatingSlider(stringResource(R.string.rating_facilities), state.facilities) { viewModel.updateRating("facilities", it) }
                        RatingSlider(stringResource(R.string.rating_communication), state.communication) { viewModel.updateRating("communication", it) }
                        RatingSlider(stringResource(R.string.rating_wifi), state.wifi) { viewModel.updateRating("wifi", it) }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Overall score display
                        val animatedScore by animateFloatAsState(
                            targetValue = state.overallScore.toFloat(),
                            animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
                            label = "overallScore"
                        )
                        val animatedScoreColor = scoreColor(animatedScore.toDouble())

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.review_overall),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = String.format("%.1f", animatedScore),
                                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp),
                                fontWeight = FontWeight.Bold,
                                color = animatedScoreColor
                            )
                            Text(
                                text = stringResource(R.string.review_out_of_ten),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Comment
                        Text(
                            text = stringResource(R.string.review_comment_label),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.comment,
                            onValueChange = { viewModel.updateComment(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp),
                            placeholder = {
                                Text(
                                    stringResource(R.string.review_comment_placeholder),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                        Text(
                            text = stringResource(R.string.review_comment_counter, state.comment.length),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (state.comment.length > 450) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, end = 4.dp),
                            textAlign = TextAlign.End
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Doodle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.review_doodle_label),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            val hasDoodle = state.doodleStrokes.isNotEmpty() || state.existingDoodleBase64 != null
                            if (hasDoodle) {
                                TextButton(onClick = { viewModel.clearDoodle() }) {
                                    Text(stringResource(R.string.review_doodle_clear))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                            )
                        ) {
                            if (state.existingDoodleBase64 != null && state.doodleStrokes.isEmpty()) {
                                val existingBitmap = remember(state.existingDoodleBase64) {
                                    decodeDoodle(state.existingDoodleBase64!!)
                                }
                                if (existingBitmap != null) {
                                    Image(
                                        bitmap = existingBitmap,
                                        contentDescription = stringResource(R.string.review_doodle_cd),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(3f)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Fit
                                    )
                                } else {
                                    DoodleCanvas(
                                        strokes = state.doodleStrokes,
                                        onStrokeAdded = { viewModel.addDoodleStroke(it) },
                                        onSizeMeasured = { viewModel.updateDoodleCanvasSize(it.width, it.height) }
                                    )
                                }
                            } else {
                                DoodleCanvas(
                                    strokes = state.doodleStrokes,
                                    onStrokeAdded = { viewModel.addDoodleStroke(it) },
                                    onSizeMeasured = { viewModel.updateDoodleCanvasSize(it.width, it.height) }
                                )
                            }
                        }
                        if (state.existingDoodleBase64 != null && state.doodleStrokes.isEmpty()) {
                            Text(
                                text = stringResource(R.string.review_doodle_replace),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        state.saveError?.let { err ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = err,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    } else if (state.selectedGuest == null) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.review_select_guest),
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
                if (state.selectedGuest != null && !state.isCheckingExisting) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            viewModel.submitReview(apartmentId, currentStay?.id ?: "")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = !state.isSaving,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = stringResource(if (state.foundExistingReview != null || state.isEditMode) R.string.review_update else R.string.review_submit),
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
}

@Composable
private fun RatingSlider(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    val trackColor by animateColorAsState(
        targetValue = scoreColor(value.toDouble()),
        animationSpec = spring(),
        label = "${label}TrackColor"
    )

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
                color = trackColor
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 1f..10f,
            steps = 8,
            colors = SliderDefaults.colors(
                thumbColor = trackColor,
                activeTrackColor = trackColor,
                inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        )
    }
}
