/*
 * Copyright (c) 2013-2025, Daniel Kirstenpfad, www.miataru.com
 *
 * DeviceViewModel.kt
 * miataru
 *
 * Created by Daniel Kirstenpfad on 2025-01-25.
 */

package com.miataru.ui.devices

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miataru.data.Device
import com.miataru.data.DeviceRepository
import kotlinx.coroutines.launch

enum class SortType {
    NAME, DEVICE_ID, COLOR
}

class DeviceViewModel(private val deviceRepository: DeviceRepository) : ViewModel() {
    
    private val _devices = MutableLiveData<List<Device>>()
    val devices: LiveData<List<Device>> = _devices
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val _sortType = MutableLiveData<SortType>()
    val sortType: LiveData<SortType> = _sortType
    
    private val _text = MutableLiveData<String>().apply {
        value = "Devices"
    }
    val text: LiveData<String> = _text
    
    init {
        _sortType.value = SortType.NAME
        loadDevices()
    }
    
    private fun loadDevices() {
        viewModelScope.launch {
            _isLoading.value = true
            deviceRepository.devices.collect { devices ->
                val sortedDevices = when (_sortType.value) {
                    SortType.NAME -> deviceRepository.sortDevicesByName()
                    SortType.DEVICE_ID -> deviceRepository.sortDevicesByDeviceId()
                    SortType.COLOR -> deviceRepository.sortDevicesByColor()
                    null -> devices
                }
                _devices.value = sortedDevices
                _isLoading.value = false
                _error.value = null
            }
        }
    }
    
    fun addDevice(device: Device) {
        viewModelScope.launch {
            _isLoading.value = true
            deviceRepository.addDevice(device)
                .onSuccess {
                    _isLoading.value = false
                    _error.value = null
                    // Reload devices after successful addition
                    loadDevices()
                }
                .onFailure { exception ->
                    _isLoading.value = false
                    _error.value = exception.message ?: "Failed to add device"
                }
        }
    }
    
    fun updateDevice(device: Device) {
        viewModelScope.launch {
            _isLoading.value = true
            deviceRepository.updateDevice(device)
                .onSuccess {
                    _isLoading.value = false
                    _error.value = null
                    // Reload devices after successful update
                    loadDevices()
                }
                .onFailure { exception ->
                    _isLoading.value = false
                    _error.value = exception.message ?: "Failed to update device"
                }
        }
    }
    
    fun deleteDevice(deviceId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            deviceRepository.deleteDevice(deviceId)
                .onSuccess {
                    _isLoading.value = false
                    _error.value = null
                    // Reload devices after successful deletion
                    loadDevices()
                }
                .onFailure { exception ->
                    _isLoading.value = false
                    _error.value = exception.message ?: "Failed to delete device"
                }
        }
    }
    
    fun setSortType(sortType: SortType) {
        _sortType.value = sortType
        loadDevices() // Reload with new sort order
    }
    
    fun clearError() {
        _error.value = null
    }
}
