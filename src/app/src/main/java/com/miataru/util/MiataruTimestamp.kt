package com.miataru.util

import java.time.Instant
import kotlin.math.abs
import kotlin.math.roundToLong

private const val TIMESTAMP_MILLISECONDS_THRESHOLD = 10_000_000_000L

fun toMiataruTimestampString(epochMillis: Long): String {
    return (epochMillis / 1000L).toString()
}

fun parseMiataruTimestampToEpochMillis(rawTimestamp: String): Long? {
    val trimmed = rawTimestamp.trim()
    if (trimmed.isBlank()) {
        return null
    }

    trimmed.toDoubleOrNull()?.let { numeric ->
        if (!numeric.isFinite()) {
            return null
        }
        val rounded = numeric.roundToLong()
        return if (abs(rounded) >= TIMESTAMP_MILLISECONDS_THRESHOLD) {
            rounded
        } else {
            rounded * 1000L
        }
    }

    return runCatching {
        Instant.parse(trimmed).toEpochMilli()
    }.getOrNull()
}
