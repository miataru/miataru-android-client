package com.miataru.data.remote

import com.miataru.client.MiataruAndroidClient
import com.miataru.client.model.GetLocationHistoryPayload
import com.miataru.client.model.GetVisitorHistoryPayload
import com.miataru.client.model.MiataruAllowedDevice
import com.miataru.client.model.MiataruGetDeviceSloganPayload
import com.miataru.client.model.MiataruGetDeviceSloganRequest
import com.miataru.client.model.MiataruGetLocationDevice
import com.miataru.client.model.MiataruGetLocationHistoryRequest
import com.miataru.client.model.MiataruGetLocationRequest
import com.miataru.client.model.MiataruGetVisitorHistoryRequest
import com.miataru.client.model.MiataruLocation
import com.miataru.client.model.MiataruSetAllowedDeviceListPayload
import com.miataru.client.model.MiataruSetAllowedDeviceListRequest
import com.miataru.client.model.MiataruSetDeviceKeyPayload
import com.miataru.client.model.MiataruSetDeviceKeyRequest
import com.miataru.client.model.MiataruSetDeviceSloganPayload
import com.miataru.client.model.MiataruSetDeviceSloganRequest
import com.miataru.client.model.MiataruUpdateLocationRequest
import com.miataru.client.model.RequestConfig
import com.miataru.client.model.UpdateConfig
import com.miataru.domain.model.LocationSnapshot
import com.miataru.domain.model.VisitorEvent
import com.miataru.domain.repository.SettingsRepository
import com.miataru.util.parseMiataruTimestampToEpochMillis
import com.miataru.util.toMiataruTimestampString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MiataruGatewayImpl @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : MiataruGateway {

    @Volatile
    private var cachedBaseUrl: String? = null

    @Volatile
    private var cachedClient: MiataruAndroidClient? = null

    override suspend fun updateOwnLocation(locationSnapshot: LocationSnapshot): GatewayResult<Unit> = safeGatewayCall {
        val settings = settingsRepository.currentSettings()
        val location = MiataruLocation(
            device = settings.ownDeviceId,
            deviceKey = settings.ownDeviceKey.ifBlank { null },
            timestamp = toMiataruTimestampString(locationSnapshot.timestampEpochMs),
            longitude = locationSnapshot.longitude.toString(),
            latitude = locationSnapshot.latitude.toString(),
            horizontalAccuracy = (locationSnapshot.horizontalAccuracyMeters ?: 0.0).toString(),
            speed = locationSnapshot.speedMps?.toString(),
            batteryLevel = locationSnapshot.batteryLevelPercent?.toString(),
            altitude = locationSnapshot.altitudeMeters?.toString(),
        )
        val request = MiataruUpdateLocationRequest(
            miataruConfig = UpdateConfig(
                enableLocationHistory = if (settings.enableLocationHistory) "True" else "False",
                locationDataRetentionTime = "100",
            ),
            miataruLocation = listOf(location),
        )
        client(settings.serverUrl).updateLocation(request)
        Unit
    }

    override suspend fun getLocations(
        deviceIds: List<String>,
        requestDeviceId: String,
        requestDeviceKey: String?,
    ): GatewayResult<List<LocationSnapshot>> = safeGatewayCall {
        val request = MiataruGetLocationRequest(
            miataruConfig = RequestConfig(
                requestMiataruDeviceID = requestDeviceId,
                requestMiataruDeviceKey = requestDeviceKey,
            ),
            miataruGetLocation = deviceIds.map { MiataruGetLocationDevice(device = it) },
        )
        val settings = settingsRepository.currentSettings()
        val response = client(settings.serverUrl).getLocation(request)
        response.miataruLocation.mapNotNull { location ->
            location.toDomainSnapshot()
        }
    }

    override suspend fun getLocationHistory(
        deviceId: String,
        amount: Int,
        requestDeviceId: String,
        requestDeviceKey: String?,
    ): GatewayResult<List<LocationSnapshot>> = safeGatewayCall {
        val request = MiataruGetLocationHistoryRequest(
            miataruConfig = RequestConfig(
                requestMiataruDeviceID = requestDeviceId,
                requestMiataruDeviceKey = requestDeviceKey,
            ),
            miataruGetLocationHistory = GetLocationHistoryPayload(
                device = deviceId,
                amount = amount.toString(),
            ),
        )
        val settings = settingsRepository.currentSettings()
        val response = client(settings.serverUrl).getLocationHistory(request)
        response.miataruLocation.mapNotNull { location -> location.toDomainSnapshot() }
    }

    override suspend fun getVisitorHistory(
        deviceId: String,
        amount: Int,
        requestDeviceKey: String?,
    ): GatewayResult<List<VisitorEvent>> = safeGatewayCall {
        val request = MiataruGetVisitorHistoryRequest(
            miataruGetVisitorHistory = GetVisitorHistoryPayload(
                device = deviceId,
                amount = amount.toString(),
                deviceKey = requestDeviceKey,
            ),
        )
        val settings = settingsRepository.currentSettings()
        val response = client(settings.serverUrl).getVisitorHistory(request)
        response.miataruVisitors.mapNotNull { visitor ->
            val parsedTimestamp = parseMiataruTimestampToEpochMillis(visitor.timeStamp)
                ?: return@mapNotNull null
            VisitorEvent(
                visitorDeviceId = visitor.deviceId,
                timestampEpochMs = parsedTimestamp,
                ignored = false,
            )
        }
    }

    override suspend fun setDeviceKey(
        deviceId: String,
        currentDeviceKey: String?,
        newDeviceKey: String,
    ): GatewayResult<Unit> = safeGatewayCall {
        val request = MiataruSetDeviceKeyRequest(
            miataruSetDeviceKey = MiataruSetDeviceKeyPayload(
                deviceId = deviceId,
                currentDeviceKey = currentDeviceKey,
                newDeviceKey = newDeviceKey,
            )
        )
        val settings = settingsRepository.currentSettings()
        client(settings.serverUrl).setDeviceKey(request)
        Unit
    }

    override suspend fun setAllowedDeviceList(
        ownDeviceId: String,
        ownDeviceKey: String,
        allowedDevices: List<AllowedDevicePolicy>,
    ): GatewayResult<Unit> = safeGatewayCall {
        val request = MiataruSetAllowedDeviceListRequest(
            miataruSetAllowedDeviceList = MiataruSetAllowedDeviceListPayload(
                deviceId = ownDeviceId,
                deviceKey = ownDeviceKey,
                allowedDevices = allowedDevices.map { policy ->
                    MiataruAllowedDevice(
                        deviceId = policy.deviceId,
                        hasCurrentLocationAccess = policy.hasCurrentLocationAccess,
                        hasHistoryAccess = policy.hasLocationHistoryAccess,
                    )
                },
            ),
        )
        val settings = settingsRepository.currentSettings()
        client(settings.serverUrl).setAllowedDeviceList(request)
        Unit
    }

    override suspend fun setDeviceSlogan(
        deviceId: String,
        deviceKey: String,
        slogan: String,
    ): GatewayResult<Unit> = safeGatewayCall {
        val request = MiataruSetDeviceSloganRequest(
            miataruSetDeviceSlogan = MiataruSetDeviceSloganPayload(
                deviceId = deviceId,
                deviceKey = deviceKey,
                slogan = slogan,
            ),
        )
        val settings = settingsRepository.currentSettings()
        client(settings.serverUrl).setDeviceSlogan(request)
        Unit
    }

    override suspend fun getDeviceSlogan(
        deviceId: String,
        requestDeviceId: String,
        requestDeviceKey: String,
    ): GatewayResult<String?> = safeGatewayCall {
        val request = MiataruGetDeviceSloganRequest(
            miataruGetDeviceSlogan = MiataruGetDeviceSloganPayload(
                deviceId = deviceId,
                requestDeviceId = requestDeviceId,
                requestDeviceKey = requestDeviceKey,
            ),
        )
        val settings = settingsRepository.currentSettings()
        val response = client(settings.serverUrl).getDeviceSlogan(request)
        response.miataruDeviceSlogan.slogan
    }

    private suspend fun client(baseUrl: String): MiataruAndroidClient = withContext(Dispatchers.IO) {
        val normalized = if (baseUrl.endsWith('/')) baseUrl else "$baseUrl/"
        val cached = cachedClient
        if (cached != null && cachedBaseUrl == normalized) {
            return@withContext cached
        }

        val newClient = MiataruAndroidClient.Builder()
            .baseUrl(normalized)
            .enableLogging(false)
            .build()

        cachedBaseUrl = normalized
        cachedClient = newClient
        newClient
    }

    private suspend fun <T> safeGatewayCall(block: suspend () -> T): GatewayResult<T> {
        return try {
            GatewayResult.Success(block())
        } catch (io: IOException) {
            GatewayResult.Error(
                type = MiataruErrorType.NETWORK,
                message = io.message ?: "Network error",
                cause = io,
            )
        } catch (iae: IllegalArgumentException) {
            GatewayResult.Error(
                type = MiataruErrorType.VALIDATION,
                message = iae.message ?: "Invalid request",
                cause = iae,
            )
        } catch (e: Exception) {
            val message = e.message.orEmpty().lowercase()
            val errorType = when {
                "401" in message || "403" in message || "devicekey" in message -> MiataruErrorType.AUTHENTICATION
                "500" in message || "502" in message || "503" in message -> MiataruErrorType.SERVER
                else -> MiataruErrorType.UNKNOWN
            }
            GatewayResult.Error(
                type = errorType,
                message = e.message ?: "Unknown error",
                cause = e,
            )
        }
    }
}

private fun com.miataru.client.model.MiataruLocation.toDomainSnapshot(): LocationSnapshot? {
    val parsedTimestamp = parseMiataruTimestampToEpochMillis(timestamp) ?: return null
    return runCatching {
        LocationSnapshot(
            deviceId = device,
            timestampEpochMs = parsedTimestamp,
            latitude = latitude.toDouble(),
            longitude = longitude.toDouble(),
            horizontalAccuracyMeters = horizontalAccuracy.toDoubleOrNull(),
            speedMps = speed?.toDoubleOrNull(),
            batteryLevelPercent = batteryLevel?.toDoubleOrNull(),
            altitudeMeters = altitude?.toDoubleOrNull(),
        )
    }.getOrNull()
}
