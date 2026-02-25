package com.miataru.client.model

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * Moshi round-trip and JSON structure tests for request/response models (API 1.1).
 */
class ModelsTest {

    private lateinit var moshi: Moshi

    @Before
    fun setup() {
        moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    @Test
    fun miataruLocation_roundTrip_withOptionalFields() {
        val adapter = moshi.adapter(MiataruLocation::class.java)
        val location = MiataruLocation(
            device = "dev-1",
            deviceKey = "key-optional",
            timestamp = "1441360863",
            longitude = "-4.394531",
            latitude = "41.079351",
            horizontalAccuracy = "50",
            speed = "1.5",
            batteryLevel = "0.85",
            altitude = "100.0"
        )
        val json = adapter.toJson(location)
        val back = adapter.fromJson(json)
        assertEquals(location, back)
        assert(json.contains("DeviceKey"))
        assert(json.contains("Speed"))
        assert(json.contains("BatteryLevel"))
        assert(json.contains("Altitude"))
    }

    @Test
    fun miataruLocation_roundTrip_minimal() {
        val adapter = moshi.adapter(MiataruLocation::class.java)
        val location = MiataruLocation(
            device = "dev-1",
            timestamp = "1441360863",
            longitude = "-4.394531",
            latitude = "41.079351",
            horizontalAccuracy = "50"
        )
        val json = adapter.toJson(location)
        val back = adapter.fromJson(json)
        assertEquals(location, back)
    }

    @Test
    fun requestConfig_roundTrip_withOptionalKey() {
        val adapter = moshi.adapter(RequestConfig::class.java)
        val config = RequestConfig(
            requestMiataruDeviceID = "requester-id",
            requestMiataruDeviceKey = "requester-key"
        )
        val json = adapter.toJson(config)
        val back = adapter.fromJson(json)
        assertEquals(config, back)
        assert(json.contains("RequestMiataruDeviceID"))
        assert(json.contains("RequestMiataruDeviceKey"))
    }

    @Test
    fun getVisitorHistoryPayload_roundTrip_withDeviceKey() {
        val adapter = moshi.adapter(GetVisitorHistoryPayload::class.java)
        val payload = GetVisitorHistoryPayload(
            device = "dev-1",
            amount = "10",
            deviceKey = "target-device-key"
        )
        val json = adapter.toJson(payload)
        val back = adapter.fromJson(json)
        assertEquals(payload, back)
    }

    @Test
    fun ack_roundTrip() {
        val adapter = moshi.adapter(Ack::class.java)
        val ack = Ack(miataruResponse = "ACK", miataruVerboseResponse = "OK")
        val json = adapter.toJson(ack)
        val back = adapter.fromJson(json)
        assertEquals(ack, back)
    }

    @Test
    fun miataruDeleteLocationRequest_roundTrip() {
        val adapter = moshi.adapter(MiataruDeleteLocationRequest::class.java)
        val req = MiataruDeleteLocationRequest(
            miataruDeleteLocation = MiataruDeleteLocationPayload(device = "dev-1", deviceKey = "key")
        )
        val json = adapter.toJson(req)
        val back = adapter.fromJson(json)
        assertEquals(req, back)
    }

    @Test
    fun miataruSetDeviceKeyRequest_roundTrip() {
        val adapter = moshi.adapter(MiataruSetDeviceKeyRequest::class.java)
        val req = MiataruSetDeviceKeyRequest(
            miataruSetDeviceKey = MiataruSetDeviceKeyPayload(
                deviceId = "dev-1",
                currentDeviceKey = null,
                newDeviceKey = "new-key"
            )
        )
        val json = adapter.toJson(req)
        val back = adapter.fromJson(json)
        assertEquals(req, back)
    }

    @Test
    fun miataruAllowedDevice_roundTrip() {
        val adapter = moshi.adapter(MiataruAllowedDevice::class.java)
        val device = MiataruAllowedDevice(
            deviceId = "allowed-dev",
            hasCurrentLocationAccess = true,
            hasHistoryAccess = false
        )
        val json = adapter.toJson(device)
        val back = adapter.fromJson(json)
        assertEquals(device, back)
    }

    @Test
    fun miataruGetDeviceSloganResponse_roundTrip() {
        val adapter = moshi.adapter(MiataruGetDeviceSloganResponse::class.java)
        val res = MiataruGetDeviceSloganResponse(
            miataruDeviceSlogan = MiataruDeviceSlogan(deviceId = "dev-1", slogan = "My device")
        )
        val json = adapter.toJson(res)
        val back = adapter.fromJson(json)
        assertEquals(res, back)
    }

    @Test
    fun errorResponse_roundTrip() {
        val adapter = moshi.adapter(ErrorResponse::class.java)
        val err = ErrorResponse(error = "Forbidden")
        val json = adapter.toJson(err)
        val back = adapter.fromJson(json)
        assertEquals(err, back)
    }

    @Test
    fun miataruDeleteLocationResponse_deserialize() {
        val adapter = moshi.adapter(MiataruDeleteLocationResponse::class.java)
        val json = """{"MiataruResponse":"ACK","MiataruVerboseResponse":"Deleted","MiataruDeletedCount":3}"""
        val res = adapter.fromJson(json)
        assertEquals("ACK", res?.miataruResponse)
        assertEquals(3, res?.miataruDeletedCount)
    }
}
