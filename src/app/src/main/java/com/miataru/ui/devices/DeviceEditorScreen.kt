package com.miataru.ui.devices

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DeviceEditorScreen(
    deviceLocalId: Long?,
    prefilledDeviceId: String?,
    onBack: () -> Unit,
    viewModel: DeviceEditorViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        val content = result.contents ?: return@rememberLauncherForActivityResult
        viewModel.applyQrPayload(content)
    }

    LaunchedEffect(deviceLocalId, prefilledDeviceId) {
        viewModel.load(deviceLocalId, prefilledDeviceId)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DeviceEditorEvent.Saved -> onBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (uiState.isEditing) "Edit device" else "Add device")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        if (!uiState.isLoaded) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Name") },
                singleLine = true,
            )

            OutlinedTextField(
                value = uiState.deviceId,
                onValueChange = viewModel::onDeviceIdChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Device ID") },
                singleLine = true,
            )

            OutlinedTextField(
                value = uiState.colorHex,
                onValueChange = viewModel::onColorChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Color (hex)") },
                singleLine = true,
            )

            OutlinedTextField(
                value = uiState.deviceKey,
                onValueChange = viewModel::onDeviceKeyChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Device key (optional)") },
                singleLine = true,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Allow current location access")
                Switch(
                    checked = uiState.hasCurrentLocationAccess,
                    onCheckedChange = viewModel::onHasCurrentLocationAccessChanged,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Allow history access")
                Switch(
                    checked = uiState.hasLocationHistoryAccess,
                    onCheckedChange = viewModel::onHasLocationHistoryAccessChanged,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = {
                    val options = ScanOptions()
                        .setOrientationLocked(false)
                        .setPrompt("Scan miataru:// QR code")
                    scanLauncher.launch(options)
                }) {
                    Icon(Icons.Rounded.QrCodeScanner, contentDescription = null)
                    Text(" Scan QR")
                }

                Button(
                    onClick = viewModel::save,
                    enabled = !uiState.isSaving,
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                    }
                    Text("Save")
                }
            }

            uiState.errorMessage?.let { errorMessage ->
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
                TextButton(onClick = viewModel::clearError) {
                    Text("Dismiss")
                }
            }
        }
    }
}
