package com.touristapp.feature.setup

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.touristapp.feature.admin.AdminViewModel
import com.touristapp.feature.admin.AdminLoginFlow
@Composable
fun SetupScreen(
    onApartmentSelected: (String) -> Unit
) {
    val viewModel: AdminViewModel = hiltViewModel()
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .padding(24.dp)
            ) {
                AdminLoginFlow(
                    viewModel = viewModel,
                    onApartmentSelected = onApartmentSelected,
                    onDismiss = null,
                    modifier = Modifier.padding(24.dp)
                )
            }
        }
    }
}
