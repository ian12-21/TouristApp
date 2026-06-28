package com.touristapp.feature.apartment

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.touristapp.data.model.Apartment
import com.touristapp.data.model.TransportationService

@Composable
fun TransportationSlide(
    apartment: Apartment?,
    transportationServices: List<TransportationService>
) {
    if (apartment == null) return
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        TransportContent(
            apartment = apartment,
            transportationServices = transportationServices
        )
    }
}
