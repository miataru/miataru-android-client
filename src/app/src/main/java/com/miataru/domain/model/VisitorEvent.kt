package com.miataru.domain.model

data class VisitorEvent(
    val id: Long = 0,
    val visitorDeviceId: String,
    val timestampEpochMs: Long,
    val ignored: Boolean = false,
)
