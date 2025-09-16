/*
 * Copyright (c) 2013-2025, Daniel Kirstenpfad, www.miataru.com
 *
 * DeviceRepository.kt
 * miataru
 *
 * Created by Daniel Kirstenpfad on 2025-01-25.
 */

package com.miataru.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

class DeviceRepository(private val context: Context) {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("device_preferences", Context.MODE_PRIVATE)
    
    private val gson = Gson()
    private val deviceListType = object : TypeToken<List<Device>>() {}.type
    
    private val _devices = MutableStateFlow<List<Device>>(emptyList())
    val devices: Flow<List<Device>> = _devices.asStateFlow()
    
    init {
        loadDevices()
    }
    
    private fun loadDevices() {
        val devicesJson = sharedPreferences.getString("devices", null)
        if (devicesJson != null) {
            try {
                val devicesList: List<Device> = gson.fromJson(devicesJson, deviceListType)
                _devices.value = devicesList
            } catch (e: Exception) {
                _devices.value = emptyList()
            }
        }
    }
    
    private fun saveDevices() {
        val devicesJson = gson.toJson(_devices.value)
        sharedPreferences.edit()
            .putString("devices", devicesJson)
            .apply()
    }
    
    suspend fun addDevice(device: Device): Result<Device> {
        return try {
            val newDevice = device.copy(id = UUID.randomUUID().toString())
            _devices.value = _devices.value + newDevice
            saveDevices()
            Result.success(newDevice)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateDevice(device: Device): Result<Device> {
        return try {
            val currentDevices = _devices.value.toMutableList()
            val index = currentDevices.indexOfFirst { it.id == device.id }
            if (index != -1) {
                currentDevices[index] = device
                _devices.value = currentDevices
                saveDevices()
                Result.success(device)
            } else {
                Result.failure(Exception("Device not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteDevice(deviceId: String): Result<Unit> {
        return try {
            _devices.value = _devices.value.filter { it.id != deviceId }
            saveDevices()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getDeviceById(deviceId: String): Device? {
        return _devices.value.find { it.id == deviceId }
    }
    
    fun sortDevicesByName(): List<Device> {
        return _devices.value.sortedBy { it.name.lowercase() }
    }
    
    fun sortDevicesByDeviceId(): List<Device> {
        return _devices.value.sortedBy { it.deviceId.lowercase() }
    }
    
    fun sortDevicesByColor(): List<Device> {
        return _devices.value.sortedBy { it.color }
    }
}
