package com.miataru.ui.groups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.miataru.domain.model.MapProvider

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun GroupMapScreen(
    onBack: () -> Unit,
    viewModel: GroupMapViewModel = hiltViewModel(),
) {
    val mapProvider by viewModel.mapProvider.collectAsStateWithLifecycle()
    val rows by viewModel.devicesWithLocation.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 2f)
    }

    LaunchedEffect(rows) {
        val first = rows.firstOrNull { it.latestLocation != null }?.latestLocation ?: return@LaunchedEffect
        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLngZoom(LatLng(first.latitude, first.longitude), 10f),
            durationMs = 800,
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Group map") },
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
                ) {
                    rows.forEach { row ->
                        val location = row.latestLocation ?: return@forEach
                        Marker(
                            state = MarkerState(position = LatLng(location.latitude, location.longitude)),
                            title = row.device.name,
                            snippet = row.device.deviceId,
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("MapLibre mode enabled")
                    Text("Google map rendering is disabled for cost policy compliance.")
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(rows, key = { it.device.id }) { row ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = row.device.name, style = MaterialTheme.typography.titleSmall)
                            Text(text = row.device.deviceId)
                            Text(
                                text = row.latestLocation?.let { "${it.latitude}, ${it.longitude}" } ?: "No location",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }

            error?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
                TextButton(onClick = viewModel::clearError, modifier = Modifier.padding(horizontal = 12.dp)) {
                    Text("Dismiss")
                }
            }
        }
    }
}
