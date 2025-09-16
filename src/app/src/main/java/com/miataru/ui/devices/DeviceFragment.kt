/*
 * Copyright (c) 2013-2025, Daniel Kirstenpfad, www.miataru.com
 *
 * DeviceFragment.kt
 * miataru
 *
 * Created by Daniel Kirstenpfad on 2025-01-25.
 */

package com.miataru.ui.devices

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.miataru.R
import com.miataru.data.Device
import com.miataru.data.DeviceRepository
import kotlinx.coroutines.launch

class DeviceFragment : Fragment() {
    
    private lateinit var deviceViewModel: DeviceViewModel
    private lateinit var deviceRepository: DeviceRepository
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DeviceAdapter
    private lateinit var fabAdd: View
    private lateinit var spinnerSort: Spinner
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_devices, container, false)
        
        deviceRepository = DeviceRepository(requireContext())
        deviceViewModel = ViewModelProvider(this, DeviceViewModelFactory(deviceRepository))[DeviceViewModel::class.java]
        
        setupViews(root)
        setupRecyclerView()
        setupObservers()
        setupSortSpinner()
        
        return root
    }
    
    private fun setupViews(root: View) {
        recyclerView = root.findViewById(R.id.recycler_view_devices)
        fabAdd = root.findViewById(R.id.fab_add_device)
        spinnerSort = root.findViewById(R.id.spinner_sort)
        
        fabAdd.setOnClickListener {
            showAddDeviceDialog()
        }
    }
    
    private fun setupRecyclerView() {
        adapter = DeviceAdapter(
            onEditClick = { device -> showEditDeviceDialog(device) },
            onDeleteClick = { device -> showDeleteConfirmDialog(device) }
        )
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }
    
    private fun setupObservers() {
        deviceViewModel.devices.observe(viewLifecycleOwner) { devices ->
            adapter.updateDevices(devices)
        }
        
        deviceViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                showErrorDialog(it)
                deviceViewModel.clearError()
            }
        }
        
        deviceViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Show/hide loading indicator if needed
        }
    }
    
    private fun setupSortSpinner() {
        val sortOptions = arrayOf("Name", "Device ID", "Color")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sortOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSort.adapter = spinnerAdapter
        
        spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val sortType = when (position) {
                    0 -> SortType.NAME
                    1 -> SortType.DEVICE_ID
                    2 -> SortType.COLOR
                    else -> SortType.NAME
                }
                deviceViewModel.setSortType(sortType)
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    
    private fun showAddDeviceDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_device_edit, null)
        val editName = dialogView.findViewById<EditText>(R.id.edit_device_name)
        val editDeviceId = dialogView.findViewById<EditText>(R.id.edit_device_id)
        val editColor = dialogView.findViewById<EditText>(R.id.edit_device_color)
        
        // Set default color
        editColor.setText("#2196F3")
        
        AlertDialog.Builder(requireContext())
            .setTitle("Add Device")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = editName.text.toString().trim()
                val deviceId = editDeviceId.text.toString().trim()
                val color = editColor.text.toString().trim()
                
                if (name.isNotEmpty() && deviceId.isNotEmpty()) {
                    val device = Device.createEmpty().copy(
                        name = name,
                        deviceId = deviceId,
                        color = color.ifEmpty { "#2196F3" }
                    )
                    deviceViewModel.addDevice(device)
                } else {
                    Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showEditDeviceDialog(device: Device) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_device_edit, null)
        val editName = dialogView.findViewById<EditText>(R.id.edit_device_name)
        val editDeviceId = dialogView.findViewById<EditText>(R.id.edit_device_id)
        val editColor = dialogView.findViewById<EditText>(R.id.edit_device_color)
        
        editName.setText(device.name)
        editDeviceId.setText(device.deviceId)
        editColor.setText(device.color)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Edit Device")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = editName.text.toString().trim()
                val deviceId = editDeviceId.text.toString().trim()
                val color = editColor.text.toString().trim()
                
                if (name.isNotEmpty() && deviceId.isNotEmpty()) {
                    val updatedDevice = device.copy(
                        name = name,
                        deviceId = deviceId,
                        color = color.ifEmpty { "#2196F3" }
                    )
                    deviceViewModel.updateDevice(updatedDevice)
                } else {
                    Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showDeleteConfirmDialog(device: Device) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Device")
            .setMessage("Are you sure you want to delete '${device.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                deviceViewModel.deleteDevice(device.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showErrorDialog(error: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Error")
            .setMessage(error)
            .setPositiveButton("OK", null)
            .show()
    }
}

class DeviceViewModelFactory(private val deviceRepository: DeviceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeviceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DeviceViewModel(deviceRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
