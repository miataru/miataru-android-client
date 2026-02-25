package com.miataru.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miataru.domain.model.MapProvider

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SettingsScreen(
    onRerunOnboarding: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    var serverUrlDraft by remember(settings.serverUrl) { mutableStateOf(settings.serverUrl) }
    var deviceKeyDraft by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") })
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("Tracking", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Track and report location")
                Switch(
                    checked = settings.trackAndReportLocation,
                    onCheckedChange = viewModel::setTrackingEnabled,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
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
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Enable allowed device list")
                Switch(
                    checked = settings.allowedDeviceListEnabled,
                    onCheckedChange = viewModel::setAllowedDeviceList,
                )
            }

            Text("Server", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = serverUrlDraft,
                onValueChange = { serverUrlDraft = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Miataru server URL") },
                singleLine = true,
            )
            Button(onClick = { viewModel.setServerUrl(serverUrlDraft) }) {
                Text("Save server URL")
            }

            Text("Device key", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = deviceKeyDraft,
                onValueChange = { deviceKeyDraft = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("New own device key") },
                singleLine = true,
            )
            Button(onClick = { viewModel.updateOwnDeviceKey(deviceKeyDraft) }) {
                Text("Update device key")
            }

            Text("Map provider", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = settings.mapProvider == MapProvider.GOOGLE,
                        onClick = { viewModel.setMapProvider(MapProvider.GOOGLE) },
                    )
                    Text("Google")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = settings.mapProvider == MapProvider.MAPLIBRE,
                        onClick = { viewModel.setMapProvider(MapProvider.MAPLIBRE) },
                    )
                    Text("MapLibre")
                }
            }

            Button(onClick = {
                viewModel.rerunOnboarding()
                onRerunOnboarding()
            }) {
                Text("Run onboarding again")
            }

            error?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                )
                TextButton(onClick = viewModel::clearError) {
                    Text("Dismiss")
                }
            }
        }
    }
}
