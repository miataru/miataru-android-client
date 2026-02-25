package com.miataru.client.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// Core models

@JsonClass(generateAdapter = true)
data class Ack(
    @Json(name = "MiataruResponse") val miataruResponse: String,
    @Json(name = "MiataruVerboseResponse") val miataruVerboseResponse: String? = null
)

@JsonClass(generateAdapter = true)
data class MiataruLocation(
    @Json(name = "Device") val device: String,
    @Json(name = "DeviceKey") val deviceKey: String? = null,
    @Json(name = "Timestamp") val timestamp: String,
    @Json(name = "Longitude") val longitude: String,
    @Json(name = "Latitude") val latitude: String,
    @Json(name = "HorizontalAccuracy") val horizontalAccuracy: String,
    @Json(name = "Speed") val speed: String? = null,
    @Json(name = "BatteryLevel") val batteryLevel: String? = null,
    @Json(name = "Altitude") val altitude: String? = null
)

// UpdateLocation request
@JsonClass(generateAdapter = true)
data class UpdateConfig(
    @Json(name = "EnableLocationHistory") val enableLocationHistory: String,
    @Json(name = "LocationDataRetentionTime") val locationDataRetentionTime: String
)

@JsonClass(generateAdapter = true)
data class MiataruUpdateLocationRequest(
    @Json(name = "MiataruConfig") val miataruConfig: UpdateConfig,
    @Json(name = "MiataruLocation") val miataruLocation: List<MiataruLocation>
)

// GetLocation request (API 1.1: RequestMiataruDeviceID mandatory, RequestMiataruDeviceKey optional)
@JsonClass(generateAdapter = true)
data class RequestConfig(
    @Json(name = "RequestMiataruDeviceID") val requestMiataruDeviceID: String,
    @Json(name = "RequestMiataruDeviceKey") val requestMiataruDeviceKey: String? = null
)

@JsonClass(generateAdapter = true)
data class MiataruGetLocationDevice(
    @Json(name = "Device") val device: String,
    @Json(name = "DeviceKey") val deviceKey: String? = null
)

@JsonClass(generateAdapter = true)
data class MiataruGetLocationRequest(
    @Json(name = "MiataruConfig") val miataruConfig: RequestConfig? = null,
    @Json(name = "MiataruGetLocation") val miataruGetLocation: List<MiataruGetLocationDevice>
)

@JsonClass(generateAdapter = true)
data class MiataruGetLocationResponse(
    @Json(name = "MiataruLocation") val miataruLocation: List<MiataruLocation>
)

// GetLocationHistory request/response
@JsonClass(generateAdapter = true)
data class GetLocationHistoryPayload(
    @Json(name = "Device") val device: String,
    @Json(name = "Amount") val amount: String
)

@JsonClass(generateAdapter = true)
data class MiataruGetLocationHistoryRequest(
    @Json(name = "MiataruConfig") val miataruConfig: RequestConfig? = null,
    @Json(name = "MiataruGetLocationHistory") val miataruGetLocationHistory: GetLocationHistoryPayload
)

@JsonClass(generateAdapter = true)
data class LocationHistoryServerConfig(
    @Json(name = "MaximumNumberOfLocationUpdates") val maximumNumberOfLocationUpdates: String,
    @Json(name = "AvailableDeviceLocationUpdates") val availableDeviceLocationUpdates: String
)

@JsonClass(generateAdapter = true)
data class MiataruGetLocationHistoryResponse(
    @Json(name = "MiataruServerConfig") val miataruServerConfig: LocationHistoryServerConfig,
    @Json(name = "MiataruLocation") val miataruLocation: List<MiataruLocation>
)

// GetVisitorHistory request/response (API 1.1: optional DeviceKey when device has key set)
@JsonClass(generateAdapter = true)
data class GetVisitorHistoryPayload(
    @Json(name = "Device") val device: String,
    @Json(name = "Amount") val amount: String,
    @Json(name = "DeviceKey") val deviceKey: String? = null
)

@JsonClass(generateAdapter = true)
data class MiataruGetVisitorHistoryRequest(
    @Json(name = "MiataruGetVisitorHistory") val miataruGetVisitorHistory: GetVisitorHistoryPayload
)

@JsonClass(generateAdapter = true)
data class MiataruVisitor(
    @Json(name = "DeviceID") val deviceId: String,
    @Json(name = "TimeStamp") val timeStamp: String
)

@JsonClass(generateAdapter = true)
data class VisitorHistoryServerConfig(
    @Json(name = "MaximumNumberOfVisitorHistory") val maximumNumberOfVisitorHistory: String,
    @Json(name = "AvailableVisitorHistory") val availableVisitorHistory: String
)

@JsonClass(generateAdapter = true)
data class MiataruGetVisitorHistoryResponse(
    @Json(name = "MiataruServerConfig") val miataruServerConfig: VisitorHistoryServerConfig,
    @Json(name = "MiataruVisitors") val miataruVisitors: List<MiataruVisitor>
)

// GeoJSON response
@JsonClass(generateAdapter = true)
data class GeoJsonGeometry(
    @Json(name = "type") val type: String,
    @Json(name = "coordinates") val coordinates: List<Double>
)

@JsonClass(generateAdapter = true)
data class GeoJsonProperties(
    @Json(name = "name") val name: String? = null,
    @Json(name = "timestamp") val timestamp: String? = null,
    @Json(name = "horizontalAccuracy") val horizontalAccuracy: String? = null,
    @Json(name = "speed") val speed: String? = null,
    @Json(name = "batteryLevel") val batteryLevel: String? = null,
    @Json(name = "altitude") val altitude: String? = null
)

@JsonClass(generateAdapter = true)
data class MiataruGetLocationGeoJSONResponse(
    @Json(name = "geometry") val geometry: GeoJsonGeometry,
    @Json(name = "type") val type: String,
    @Json(name = "properties") val properties: GeoJsonProperties
)

// DeleteLocation (API 1.1)
@JsonClass(generateAdapter = true)
data class MiataruDeleteLocationPayload(
    @Json(name = "Device") val device: String,
    @Json(name = "DeviceKey") val deviceKey: String? = null
)

@JsonClass(generateAdapter = true)
data class MiataruDeleteLocationRequest(
    @Json(name = "MiataruDeleteLocation") val miataruDeleteLocation: MiataruDeleteLocationPayload
)

@JsonClass(generateAdapter = true)
data class MiataruDeleteLocationResponse(
    @Json(name = "MiataruResponse") val miataruResponse: String,
    @Json(name = "MiataruVerboseResponse") val miataruVerboseResponse: String? = null,
    @Json(name = "MiataruDeletedCount") val miataruDeletedCount: Int? = null
)

// SetDeviceKey (API 1.1)
@JsonClass(generateAdapter = true)
data class MiataruSetDeviceKeyPayload(
    @Json(name = "DeviceID") val deviceId: String,
    @Json(name = "CurrentDeviceKey") val currentDeviceKey: String? = null,
    @Json(name = "NewDeviceKey") val newDeviceKey: String
)

@JsonClass(generateAdapter = true)
data class MiataruSetDeviceKeyRequest(
    @Json(name = "MiataruSetDeviceKey") val miataruSetDeviceKey: MiataruSetDeviceKeyPayload
)

@JsonClass(generateAdapter = true)
data class MiataruSetDeviceKeyResponse(
    @Json(name = "MiataruResponse") val miataruResponse: String,
    @Json(name = "MiataruVerboseResponse") val miataruVerboseResponse: String? = null
)

// SetAllowedDeviceList (API 1.1)
@JsonClass(generateAdapter = true)
data class MiataruAllowedDevice(
    @Json(name = "DeviceID") val deviceId: String,
    @Json(name = "hasCurrentLocationAccess") val hasCurrentLocationAccess: Boolean = false,
    @Json(name = "hasHistoryAccess") val hasHistoryAccess: Boolean = false
)

@JsonClass(generateAdapter = true)
data class MiataruSetAllowedDeviceListPayload(
    @Json(name = "DeviceID") val deviceId: String,
    @Json(name = "DeviceKey") val deviceKey: String,
    @Json(name = "allowedDevices") val allowedDevices: List<MiataruAllowedDevice>
)

@JsonClass(generateAdapter = true)
data class MiataruSetAllowedDeviceListRequest(
    @Json(name = "MiataruSetAllowedDeviceList") val miataruSetAllowedDeviceList: MiataruSetAllowedDeviceListPayload
)

@JsonClass(generateAdapter = true)
data class MiataruSetAllowedDeviceListResponse(
    @Json(name = "MiataruResponse") val miataruResponse: String,
    @Json(name = "MiataruVerboseResponse") val miataruVerboseResponse: String? = null
)

// SetDeviceSlogan (API 1.1)
@JsonClass(generateAdapter = true)
data class MiataruSetDeviceSloganPayload(
    @Json(name = "DeviceID") val deviceId: String,
    @Json(name = "DeviceKey") val deviceKey: String,
    @Json(name = "Slogan") val slogan: String
)

@JsonClass(generateAdapter = true)
data class MiataruSetDeviceSloganRequest(
    @Json(name = "MiataruSetDeviceSlogan") val miataruSetDeviceSlogan: MiataruSetDeviceSloganPayload
)

@JsonClass(generateAdapter = true)
data class MiataruSetDeviceSloganResponse(
    @Json(name = "MiataruResponse") val miataruResponse: String,
    @Json(name = "MiataruVerboseResponse") val miataruVerboseResponse: String? = null
)

// GetDeviceSlogan (API 1.1)
@JsonClass(generateAdapter = true)
data class MiataruGetDeviceSloganPayload(
    @Json(name = "DeviceID") val deviceId: String,
    @Json(name = "RequestDeviceID") val requestDeviceId: String,
    @Json(name = "RequestDeviceKey") val requestDeviceKey: String
)

@JsonClass(generateAdapter = true)
data class MiataruGetDeviceSloganRequest(
    @Json(name = "MiataruGetDeviceSlogan") val miataruGetDeviceSlogan: MiataruGetDeviceSloganPayload
)

@JsonClass(generateAdapter = true)
data class MiataruDeviceSlogan(
    @Json(name = "DeviceID") val deviceId: String,
    @Json(name = "Slogan") val slogan: String? = null
)

@JsonClass(generateAdapter = true)
data class MiataruGetDeviceSloganResponse(
    @Json(name = "MiataruDeviceSlogan") val miataruDeviceSlogan: MiataruDeviceSlogan
)

// Error response for 4xx/5xx
@JsonClass(generateAdapter = true)
data class ErrorResponse(
    @Json(name = "error") val error: String
)
