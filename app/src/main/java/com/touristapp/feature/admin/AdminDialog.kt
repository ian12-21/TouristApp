package com.touristapp.feature.admin

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.touristapp.feature.admin.AdminViewModel
@Composable
fun AdminDialog(
    onApartmentSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onLockout: () -> Unit
) {
    val viewModel: AdminViewModel = hiltViewModel()
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.widthIn(max = 400.dp)
        ) {
            AdminLoginFlow(
                viewModel = viewModel,
                onApartmentSelected = onApartmentSelected,
                onDismiss = onDismiss,
                onLockout = onLockout,
                modifier = Modifier.padding(24.dp)
            )
        }
    }
}
