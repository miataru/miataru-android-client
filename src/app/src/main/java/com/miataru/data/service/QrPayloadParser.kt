package com.miataru.data.service

import android.net.Uri
import javax.inject.Inject
import javax.inject.Singleton

sealed interface QrPayloadParseResult {
    data class Valid(val deviceId: String) : QrPayloadParseResult
    data class Invalid(val reason: String) : QrPayloadParseResult
}

@Singleton
class QrPayloadParser @Inject constructor() {
    fun parse(rawInput: String): QrPayloadParseResult {
        val raw = rawInput.trim()
        if (raw.isBlank()) {
            return QrPayloadParseResult.Invalid("QR payload is empty")
        }

        if (raw.startsWith(MIATARU_SCHEME_PREFIX, ignoreCase = true)) {
            val uri = Uri.parse(raw)
            val deviceId = extractDeviceId(uri)
                ?: return QrPayloadParseResult.Invalid("Missing device ID in miataru:// URI")
            return validateDeviceId(deviceId)
        }

        return validateDeviceId(raw)
    }

    private fun extractDeviceId(uri: Uri): String? {
        val host = uri.host?.trim().orEmpty()
        if (host.isNotBlank()) {
            return host
        }

        val ssp = uri.schemeSpecificPart.orEmpty()
        return ssp.removePrefix("//")
            .substringBefore('/')
            .substringBefore('?')
            .trim()
            .takeIf { it.isNotBlank() }
    }

    private fun validateDeviceId(candidate: String): QrPayloadParseResult {
        val normalized = candidate.trim()
        return if (DEVICE_ID_REGEX.matches(normalized)) {
            QrPayloadParseResult.Valid(deviceId = normalized)
        } else {
            QrPayloadParseResult.Invalid("Invalid device ID format")
        }
    }

    companion object {
        private const val MIATARU_SCHEME_PREFIX = "miataru://"
        private val DEVICE_ID_REGEX = Regex("^[A-Za-z0-9-]{6,128}$")
    }
}
