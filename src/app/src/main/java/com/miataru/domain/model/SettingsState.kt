package com.miataru.domain.model

data class SettingsState(
    val onboardingCompleted: Boolean = false,
    val trackAndReportLocation: Boolean = false,
    val enableLocationHistory: Boolean = false,
    val allowedDeviceListEnabled: Boolean = false,
    val serverUrl: String = "https://service.miataru.com",
    val ownDeviceId: String = "",
    val ownDeviceKey: String = "",
    val mapProvider: MapProvider = MapProvider.GOOGLE,
)
