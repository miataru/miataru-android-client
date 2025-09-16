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
    @Json(name = "Timestamp") val timestamp: String,
    @Json(name = "Longitude") val longitude: String,
    @Json(name = "Latitude") val latitude: String,
    @Json(name = "HorizontalAccuracy") val horizontalAccuracy: String
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

// GetLocation request
@JsonClass(generateAdapter = true)
data class RequestConfig(
    @Json(name = "RequestMiataruDeviceID") val requestMiataruDeviceID: String
)

@JsonClass(generateAdapter = true)
data class MiataruGetLocationDevice(
    @Json(name = "Device") val device: String
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

// GetVisitorHistory request/response
@JsonClass(generateAdapter = true)
data class GetVisitorHistoryPayload(
    @Json(name = "Device") val device: String,
    @Json(name = "Amount") val amount: String
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
    @Json(name = "name") val name: String
)

@JsonClass(generateAdapter = true)
data class MiataruGetLocationGeoJSONResponse(
    @Json(name = "geometry") val geometry: GeoJsonGeometry,
    @Json(name = "type") val type: String,
    @Json(name = "properties") val properties: GeoJsonProperties
)
