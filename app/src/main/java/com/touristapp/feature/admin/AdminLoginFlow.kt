package com.touristapp.feature.admin

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.touristapp.R
import com.touristapp.data.model.Pairing

@Composable
fun AdminLoginFlow(
    viewModel: AdminViewModel,
    onApartmentSelected: (Pairing) -> Unit,
    onDismiss: (() -> Unit)?,
    onLockout: (() -> Unit)? = null,
    isKioskEnabled: Boolean = false,
    onExitKiosk: () -> Unit = {},
    onEnableKiosk: () -> Unit = {},
    /** The apartment the tablet shows now; the picker marks it. */
    pairedApartmentId: String? = null,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Once logged in, the admin dialog (onDismiss != null) shows an action menu first;
    // the setup screen (onDismiss == null) jumps straight to the apartment picker.
    var showApartmentPicker by remember { mutableStateOf(onDismiss == null) }

    Column(modifier = modifier) {
        val title = when {
            !state.isAuthenticated -> R.string.admin_title_sign_in
            showApartmentPicker -> R.string.admin_select_apartment
            else -> R.string.admin_title_menu
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            if (onDismiss != null) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cd_close))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            !state.isAuthenticated -> {
                OutlinedTextField(
                    value = state.email,
                    onValueChange = { viewModel.updateEmail(it) },
                    label = { Text(stringResource(R.string.admin_email)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.password,
                    onValueChange = { viewModel.updatePassword(it) },
                    label = { Text(stringResource(R.string.admin_password)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.signIn {
                            onLockout?.invoke()
                            onDismiss?.invoke()
                        }
                    },
                    enabled = !state.isLoading && state.email.isNotBlank() && state.password.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(stringResource(R.string.admin_sign_in))
                    }
                }

                state.error?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(error.messageRes()),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            showApartmentPicker -> {
                val apartments = state.apartments.orEmpty()
                if (apartments.isEmpty()) {
                    NoApartmentsMessage(
                        onSignOut = {
                            viewModel.lock()
                            // Back to the action menu after the next sign-in, as on open.
                            showApartmentPicker = onDismiss == null
                        }
                    )
                }

                LazyColumn {
                    items(apartments) { choice ->
                        ApartmentChoiceCard(
                            label = choice.label,
                            isPaired = choice.pairing.apartmentId == pairedApartmentId,
                            onClick = { viewModel.selectApartment(choice.pairing, onApartmentSelected) }
                        )
                    }
                }
            }

            else -> {
                AdminActionMenu(
                    isKioskEnabled = isKioskEnabled,
                    onReconfigure = { showApartmentPicker = true },
                    onExitKiosk = onExitKiosk,
                    onEnableKiosk = onEnableKiosk
                )
            }
        }
    }
}

/** One apartment in the picker; the one the tablet shows now is highlighted and labelled. */
@Composable
private fun ApartmentChoiceCard(
    label: String,
    isPaired: Boolean,
    onClick: () -> Unit
) {
    Card(
        colors = if (isPaired) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        },
        // The dialog is a card of the same colour, so the others need a border to read as cards.
        border = if (isPaired) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            if (isPaired) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.admin_current_apartment),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun AdminActionMenu(
    isKioskEnabled: Boolean,
    onReconfigure: () -> Unit,
    onExitKiosk: () -> Unit,
    onEnableKiosk: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = onReconfigure,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.admin_menu_reconfigure))
        }

        if (isKioskEnabled) {
            OutlinedButton(
                onClick = onExitKiosk,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.admin_menu_exit_kiosk))
            }
        } else {
            OutlinedButton(
                onClick = onEnableKiosk,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.admin_menu_enable_kiosk))
            }
        }
    }
}

/**
 * Shown instead of the picker when the owner has no apartments yet. The sign-out button
 * matters on the setup screen, where the flow has no close button: without it the owner
 * could not switch to another account.
 */
@Composable
private fun NoApartmentsMessage(onSignOut: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.admin_no_apartments),
            style = MaterialTheme.typography.bodyMedium
        )
        OutlinedButton(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.admin_sign_out))
        }
    }
}

@StringRes
private fun AdminError.messageRes(): Int = when (this) {
    AdminError.INVALID_CREDENTIALS -> R.string.admin_invalid_credentials
    AdminError.NO_NETWORK -> R.string.admin_no_network
    AdminError.NOT_ACTIVATED -> R.string.admin_not_activated
    AdminError.LOAD_APARTMENTS_FAILED -> R.string.admin_load_apartments_failed
}
