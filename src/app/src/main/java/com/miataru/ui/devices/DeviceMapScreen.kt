package com.miataru.ui.devices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.miataru.domain.model.MapProvider

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DeviceMapScreen(
    onBack: () -> Unit,
    viewModel: DeviceMapViewModel = hiltViewModel(),
) {
    val mapProvider by viewModel.mapProvider.collectAsStateWithLifecycle()
    val location by viewModel.location.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 2f)
    }

    LaunchedEffect(viewModel.deviceId) {
        viewModel.refresh()
    }

    LaunchedEffect(location) {
        val latest = location ?: return@LaunchedEffect
        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLngZoom(LatLng(latest.latitude, latest.longitude), 14f),
            durationMs = 800,
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Device map") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Refresh")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (mapProvider == MapProvider.GOOGLE) {
                GoogleMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = false),
                ) {
                    val latest = location
                    if (latest != null) {
                        val latLng = LatLng(latest.latitude, latest.longitude)
                        Marker(
                            state = MarkerState(position = latLng),
                            title = viewModel.deviceId,
                            snippet = "Last reported device location",
                        )
                        latest.horizontalAccuracyMeters?.let { accuracy ->
                            Circle(
                                center = latLng,
                                radius = accuracy,
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "MapLibre mode enabled",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text("Google Maps rendering is disabled by policy. Integrate MapLibre renderer for full map parity.")
                    location?.let {
                        Text("Latest location: ${it.latitude}, ${it.longitude}")
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Device ID: ${viewModel.deviceId}")
                    location?.let {
                        Text("Lat/Lon: ${it.latitude}, ${it.longitude}")
                        Text("Timestamp: ${it.timestampEpochMs}")
                    } ?: Text("No location available")
                }
            }

            error?.let { message ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = viewModel::clearError) {
                        Text("Dismiss")
                    }
                }
            }
        }
    }
}
