package com.miataru.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miataru.domain.model.MapProvider

@Composable
fun OnboardingScreen(
    onCompleted: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var step by remember { mutableIntStateOf(0) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        val granted = (result[Manifest.permission.ACCESS_FINE_LOCATION] == true) ||
            (result[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
        if (granted) {
            viewModel.retryTrackingActivationIfEnabled()
        }
    }

    val backgroundLocationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            viewModel.retryTrackingActivationIfEnabled()
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            viewModel.retryTrackingActivationIfEnabled()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Miataru Setup",
            style = MaterialTheme.typography.headlineSmall,
        )

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                when (step) {
                    0 -> {
                        Text(
                            text = "Welcome",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text("This onboarding configures tracking, server connection, and access policies.")
                    }

                    1 -> {
                        Text(
                            text = "Location permissions",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("Track and report location")
                            Switch(
                                checked = settings.trackAndReportLocation,
                                onCheckedChange = viewModel::setTrackAndReportLocation,
                            )
                        }
                        Button(onClick = {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                )
                            )
                        }) {
                            Text("Grant location permissions")
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            TextButton(onClick = {
                                backgroundLocationPermissionLauncher.launch(
                                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                                )
                            }) {
                                Text("Allow background location")
                            }
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            TextButton(onClick = {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }) {
                                Text("Allow tracking notifications")
                            }
                        }
                    }

                    2 -> {
                        Text(
                            text = "Server",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        OutlinedTextField(
                            value = settings.serverUrl,
                            onValueChange = viewModel::setServerUrl,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Miataru server URL") },
                            singleLine = true,
                        )
                    }

                    3 -> {
                        Text(
                            text = "History and ACL",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("Enable location history")
                            Switch(
                                checked = settings.enableLocationHistory,
                                onCheckedChange = viewModel::setEnableLocationHistory,
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("Enable allowed device list")
                            Switch(
                                checked = settings.allowedDeviceListEnabled,
                                onCheckedChange = viewModel::setAllowedDeviceListEnabled,
                            )
                        }
                    }

                    4 -> {
                        Text(
                            text = "Map provider",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text("Google Maps is default. Switch to MapLibre if cost policy requires it.")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                RadioButton(
                                    selected = settings.mapProvider == MapProvider.GOOGLE,
                                    onClick = { viewModel.setMapProvider(MapProvider.GOOGLE) },
                                )
                                Text("Google")
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                RadioButton(
                                    selected = settings.mapProvider == MapProvider.MAPLIBRE,
                                    onClick = { viewModel.setMapProvider(MapProvider.MAPLIBRE) },
                                )
                                Text("MapLibre")
                            }
                        }
                    }

                    5 -> {
                        Text(
                            text = "Ready",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text("Finish onboarding to start using Miataru on Android.")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButton(
                onClick = { if (step > 0) step -= 1 },
                enabled = step > 0,
            ) {
                Text("Back")
            }

            if (step < 5) {
                Button(onClick = { step += 1 }) {
                    Text("Next")
                }
            } else {
                Button(onClick = {
                    viewModel.completeOnboarding(onComplete = onCompleted)
                }) {
                    Text("Finish")
                }
            }
        }
    }
}
