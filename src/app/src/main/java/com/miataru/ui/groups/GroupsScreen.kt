package com.miataru.ui.groups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.People
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miataru.domain.model.Device
import com.miataru.domain.model.DeviceGroup

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun GroupsScreen(
    onOpenGroupMap: (Long) -> Unit,
    viewModel: GroupsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var groupName by remember { mutableStateOf("") }

    var editingMembersGroup by remember { mutableStateOf<DeviceGroup?>(null) }
    val selectedMembers = remember { mutableStateListOf<Long>() }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Groups") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                groupName = ""
                showAddDialog = true
            }) {
                Icon(Icons.Rounded.Add, contentDescription = "Add group")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            uiState.errorMessage?.let { error ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = viewModel::clearError) {
                        Text("Dismiss")
                    }
                }
            }

            if (uiState.groups.isEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("No groups yet. Create one to organize devices.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.groups, key = { it.id }) { group ->
                        GroupCard(
                            group = group,
                            onMap = { onOpenGroupMap(group.id) },
                            onMembers = {
                                selectedMembers.clear()
                                selectedMembers.addAll(group.memberDeviceIds)
                                editingMembersGroup = group
                            },
                            onDelete = { viewModel.deleteGroup(group.id) },
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add group") },
            text = {
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Group name") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (groupName.isNotBlank()) {
                        viewModel.addGroup(groupName)
                    }
                    showAddDialog = false
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    editingMembersGroup?.let { group ->
        MembersDialog(
            group = group,
            allDevices = uiState.devices,
            selectedMembers = selectedMembers,
            onDismiss = { editingMembersGroup = null },
            onSave = {
                viewModel.updateGroupMembers(group.id, selectedMembers.toList())
                editingMembersGroup = null
            },
        )
    }
}

@Composable
private fun GroupCard(
    group: DeviceGroup,
    onMap: () -> Unit,
    onMembers: () -> Unit,
    onDelete: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = group.name, style = MaterialTheme.typography.titleMedium)
            Text(text = "Members: ${group.memberDeviceIds.size}")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(onClick = onMembers) {
                    Icon(Icons.Rounded.People, contentDescription = "Edit members")
                }
                IconButton(onClick = onMap) {
                    Icon(Icons.Rounded.Map, contentDescription = "Open group map")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Rounded.Delete, contentDescription = "Delete group")
                }
            }
        }
    }
}

@Composable
private fun MembersDialog(
    group: DeviceGroup,
    allDevices: List<Device>,
    selectedMembers: MutableList<Long>,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Members: ${group.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                allDevices.filter { !it.isOwnDevice }.forEach { device ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(text = device.name)
                        Checkbox(
                            checked = device.id in selectedMembers,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    if (device.id !in selectedMembers) {
                                        selectedMembers.add(device.id)
                                    }
                                } else {
                                    selectedMembers.remove(device.id)
                                }
                            },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
