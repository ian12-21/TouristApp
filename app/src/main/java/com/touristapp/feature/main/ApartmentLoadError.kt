package com.touristapp.feature.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DomainDisabled
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.touristapp.R
import com.touristapp.core.ui.components.ErrorContent
import com.touristapp.core.util.ErrorReason

/**
 * Full-screen state for when the paired apartment can't be shown. Without the apartment
 * there is no top bar, so the hidden owner gesture is gone; the owner button is the way
 * back to the pairing flow, which a kiosk tablet otherwise couldn't reach.
 */
@Composable
fun ApartmentLoadError(
    reason: ErrorReason,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {},
    onOwnerSignIn: () -> Unit = {}
) {
    val ownerSignIn: @Composable () -> Unit = {
        TextButton(onClick = onOwnerSignIn) {
            Text(stringResource(R.string.owner_sign_in))
        }
    }
    when (reason) {
        ErrorReason.OFFLINE -> ErrorContent(
            message = stringResource(R.string.error_no_internet),
            modifier = modifier,
            icon = Icons.Default.WifiOff,
            onRetry = onRetry
        )
        ErrorReason.NOT_FOUND -> ErrorContent(
            message = stringResource(R.string.error_apartment_removed),
            modifier = modifier,
            icon = Icons.Default.DomainDisabled,
            onRetry = onRetry,
            footer = ownerSignIn
        )
        ErrorReason.OTHER -> ErrorContent(
            message = stringResource(R.string.error_generic),
            modifier = modifier,
            onRetry = onRetry,
            footer = ownerSignIn
        )
    }
}

@Preview
@Composable
private fun PreviewApartmentLoadError() {
    MaterialTheme {
        Surface {
            ApartmentLoadError(reason = ErrorReason.NOT_FOUND)
        }
    }
}
