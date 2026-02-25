package com.miataru.data.service

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class QrPayloadParserTest {

    private val parser = QrPayloadParser()

    @Test
    fun `parse valid miataru uri`() {
        val result = parser.parse("miataru://abc-123-xyz")

        assertThat(result).isInstanceOf(QrPayloadParseResult.Valid::class.java)
        result as QrPayloadParseResult.Valid
        assertThat(result.deviceId).isEqualTo("abc-123-xyz")
    }

    @Test
    fun `parse plain device id`() {
        val result = parser.parse("A1B2C3D4")

        assertThat(result).isInstanceOf(QrPayloadParseResult.Valid::class.java)
        result as QrPayloadParseResult.Valid
        assertThat(result.deviceId).isEqualTo("A1B2C3D4")
    }

    @Test
    fun `reject invalid payload`() {
        val result = parser.parse("https://example.com")

        assertThat(result).isInstanceOf(QrPayloadParseResult.Invalid::class.java)
    }
}
