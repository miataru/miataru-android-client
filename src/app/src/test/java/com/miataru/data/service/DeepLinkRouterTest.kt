package com.miataru.data.service

import android.net.Uri
import com.google.common.truth.Truth.assertThat
import com.miataru.domain.model.Device
import com.miataru.domain.model.DeviceWithLocation
import com.miataru.domain.model.LocationSnapshot
import com.miataru.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Test

class DeepLinkRouterTest {

    @Test
    fun `resolve existing device to map route`() = runBlocking {
        val repository = FakeDeviceRepository(
            existingByDeviceId = mapOf(
                "known-device" to Device(
                    id = 1,
                    name = "Known",
                    deviceId = "known-device",
                )
            )
        )
        val router = DeepLinkRouter(repository, QrPayloadParser())

        val result = router.resolve(Uri.parse("miataru://known-device"))

        assertThat(result).isEqualTo(DeepLinkResolution.OpenDeviceMap("known-device"))
    }

    @Test
    fun `resolve unknown device to add route`() = runBlocking {
        val router = DeepLinkRouter(FakeDeviceRepository(), QrPayloadParser())

        val result = router.resolve(Uri.parse("miataru://new-device-123"))

        assertThat(result).isEqualTo(DeepLinkResolution.OpenAddDevice("new-device-123"))
    }

    @Test
    fun `ignore non miataru scheme`() = runBlocking {
        val router = DeepLinkRouter(FakeDeviceRepository(), QrPayloadParser())

        val result = router.resolve(Uri.parse("https://miataru.com"))

        assertThat(result).isEqualTo(DeepLinkResolution.Ignore)
    }

    private class FakeDeviceRepository(
        private val existingByDeviceId: Map<String, Device> = emptyMap(),
    ) : DeviceRepository {
        override val devices: Flow<List<Device>> = MutableStateFlow(existingByDeviceId.values.toList())
        override val devicesWithLocation: Flow<List<DeviceWithLocation>> = flowOf(emptyList())

        override suspend fun ensureOwnDeviceExists() = Unit

        override suspend fun addDevice(device: Device): Result<Device> = Result.success(device)

        override suspend fun updateDevice(device: Device): Result<Device> = Result.success(device)

        override suspend fun deleteDevice(deviceId: Long): Result<Unit> = Result.success(Unit)

        override suspend fun getById(deviceId: Long): Device? = existingByDeviceId.values.firstOrNull { it.id == deviceId }

        override suspend fun getByDeviceId(deviceId: String): Device? = existingByDeviceId[deviceId]

        override fun observeLatestLocation(deviceId: String): Flow<LocationSnapshot?> = flowOf(null)

        override suspend fun refreshLocations(targetDeviceIds: List<String>): Result<Unit> = Result.success(Unit)
    }
}
