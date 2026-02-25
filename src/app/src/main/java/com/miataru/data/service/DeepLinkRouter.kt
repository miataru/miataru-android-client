package com.miataru.data.service

import android.net.Uri
import com.miataru.domain.repository.DeviceRepository
import javax.inject.Inject
import javax.inject.Singleton

sealed interface DeepLinkResolution {
    data object Ignore : DeepLinkResolution
    data class OpenDeviceMap(val deviceId: String) : DeepLinkResolution
    data class OpenAddDevice(val prefilledDeviceId: String) : DeepLinkResolution
}

@Singleton
class DeepLinkRouter @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val qrPayloadParser: QrPayloadParser,
) {

    suspend fun resolve(uri: Uri?): DeepLinkResolution {
        if (uri == null || !uri.isMiataruScheme()) {
            return DeepLinkResolution.Ignore
        }

        val candidate = uri.host?.takeIf { it.isNotBlank() }
            ?: uri.schemeSpecificPart
                ?.removePrefix("//")
                ?.substringBefore('/')
                ?.substringBefore('?')
                ?.takeIf { it.isNotBlank() }
            ?: return DeepLinkResolution.Ignore

        return when (val parseResult = qrPayloadParser.parse("miataru://$candidate")) {
            is QrPayloadParseResult.Invalid -> DeepLinkResolution.Ignore
            is QrPayloadParseResult.Valid -> {
                val existing = deviceRepository.getByDeviceId(parseResult.deviceId)
                if (existing != null) {
                    DeepLinkResolution.OpenDeviceMap(existing.deviceId)
                } else {
                    DeepLinkResolution.OpenAddDevice(parseResult.deviceId)
                }
            }
        }
    }

    private fun Uri.isMiataruScheme(): Boolean = scheme.equals("miataru", ignoreCase = true)
}
