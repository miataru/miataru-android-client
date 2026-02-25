package com.miataru.ui.main

import android.net.Uri

object AppRoutes {
    const val ONBOARDING = "onboarding"
    const val DEVICES = "devices"
    const val ADD_DEVICE = "device/add"
    const val EDIT_DEVICE = "device/edit/{deviceLocalId}"
    const val DEVICE_MAP = "device/map/{deviceId}"
    const val GROUPS = "groups"
    const val GROUP_MAP = "groups/map/{groupId}"
    const val SETTINGS = "settings"
    const val VISITORS = "visitors"

    const val ARG_DEVICE_LOCAL_ID = "deviceLocalId"
    const val ARG_DEVICE_ID = "deviceId"
    const val ARG_GROUP_ID = "groupId"
    const val ARG_PREFILL_DEVICE_ID = "prefillDeviceId"

    const val ADD_DEVICE_WITH_PREFILL = "$ADD_DEVICE?$ARG_PREFILL_DEVICE_ID={$ARG_PREFILL_DEVICE_ID}"

    fun addDevice(prefilledDeviceId: String? = null): String {
        val encoded = prefilledDeviceId?.takeIf { it.isNotBlank() }?.let(Uri::encode)
        return if (encoded == null) {
            ADD_DEVICE
        } else {
            "$ADD_DEVICE?$ARG_PREFILL_DEVICE_ID=$encoded"
        }
    }

    fun editDevice(deviceLocalId: Long): String = "device/edit/$deviceLocalId"

    fun deviceMap(deviceId: String): String = "device/map/${Uri.encode(deviceId)}"

    fun groupMap(groupId: Long): String = "groups/map/$groupId"
}
