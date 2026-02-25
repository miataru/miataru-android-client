package com.miataru.data.remote

import com.miataru.domain.model.LocationSnapshot
import com.miataru.domain.model.VisitorEvent

enum class MiataruErrorType {
    NETWORK,
    AUTHENTICATION,
    VALIDATION,
    SERVER,
    UNKNOWN,
}

sealed interface GatewayResult<out T> {
    data class Success<T>(val value: T) : GatewayResult<T>

    data class Error(
        val type: MiataruErrorType,
        val message: String,
        val cause: Throwable? = null,
    ) : GatewayResult<Nothing>
}

interface MiataruGateway {
    suspend fun updateOwnLocation(locationSnapshot: LocationSnapshot): GatewayResult<Unit>
    suspend fun getLocations(deviceIds: List<String>, requestDeviceId: String, requestDeviceKey: String?): GatewayResult<List<LocationSnapshot>>
    suspend fun getLocationHistory(deviceId: String, amount: Int, requestDeviceId: String, requestDeviceKey: String?): GatewayResult<List<LocationSnapshot>>
    suspend fun getVisitorHistory(deviceId: String, amount: Int, requestDeviceKey: String?): GatewayResult<List<VisitorEvent>>
    suspend fun setDeviceKey(deviceId: String, currentDeviceKey: String?, newDeviceKey: String): GatewayResult<Unit>
    suspend fun setAllowedDeviceList(
        ownDeviceId: String,
        ownDeviceKey: String,
        allowedDevices: List<AllowedDevicePolicy>,
    ): GatewayResult<Unit>
    suspend fun setDeviceSlogan(deviceId: String, deviceKey: String, slogan: String): GatewayResult<Unit>
    suspend fun getDeviceSlogan(deviceId: String, requestDeviceId: String, requestDeviceKey: String): GatewayResult<String?>
}

data class AllowedDevicePolicy(
    val deviceId: String,
    val hasCurrentLocationAccess: Boolean,
    val hasLocationHistoryAccess: Boolean,
)
