package com.miataru.client

import com.miataru.client.model.*
import com.miataru.client.network.MiataruService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class MiataruAndroidClient private constructor(
    private val service: MiataruService
) {

    suspend fun updateLocation(body: MiataruUpdateLocationRequest): Ack = service.updateLocation(body)

    suspend fun getLocation(body: MiataruGetLocationRequest): MiataruGetLocationResponse = service.getLocation(body)

    suspend fun getLocationGeoJSON(deviceId: String): MiataruGetLocationGeoJSONResponse = service.getLocationGeoJSON(deviceId)

    suspend fun getLocationHistory(body: MiataruGetLocationHistoryRequest): MiataruGetLocationHistoryResponse = service.getLocationHistory(body)

    suspend fun getVisitorHistory(body: MiataruGetVisitorHistoryRequest): MiataruGetVisitorHistoryResponse = service.getVisitorHistory(body)

    class Builder {
        private var baseUrl: String = DEFAULT_BASE_URL
        private var connectTimeoutSeconds: Long = 15
        private var readTimeoutSeconds: Long = 30
        private var writeTimeoutSeconds: Long = 30
        private var enableHttpLogging: Boolean = false
        private val networkInterceptors: MutableList<Interceptor> = mutableListOf()
        private val applicationInterceptors: MutableList<Interceptor> = mutableListOf()

        fun baseUrl(url: String) = apply { this.baseUrl = url }
        fun timeouts(connect: Long = 15, read: Long = 30, write: Long = 30) = apply {
            this.connectTimeoutSeconds = connect
            this.readTimeoutSeconds = read
            this.writeTimeoutSeconds = write
        }
        fun enableLogging(enable: Boolean) = apply { this.enableHttpLogging = enable }
        fun addNetworkInterceptor(interceptor: Interceptor) = apply { this.networkInterceptors.add(interceptor) }
        fun addApplicationInterceptor(interceptor: Interceptor) = apply { this.applicationInterceptors.add(interceptor) }

        fun build(): MiataruAndroidClient {
            val moshi: Moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            val okHttpBuilder = OkHttpClient.Builder()
                .connectTimeout(connectTimeoutSeconds, TimeUnit.SECONDS)
                .readTimeout(readTimeoutSeconds, TimeUnit.SECONDS)
                .writeTimeout(writeTimeoutSeconds, TimeUnit.SECONDS)

            if (enableHttpLogging) {
                val logging = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
                okHttpBuilder.addInterceptor(logging)
            }

            applicationInterceptors.forEach { okHttpBuilder.addInterceptor(it) }
            networkInterceptors.forEach { okHttpBuilder.addNetworkInterceptor(it) }

            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(ensureEndsWithSlash(baseUrl))
                .client(okHttpBuilder.build())
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            val service = retrofit.create(MiataruService::class.java)
            return MiataruAndroidClient(service)
        }

        private fun ensureEndsWithSlash(url: String): String = if (url.endsWith('/')) url else "$url/"
    }

    companion object {
        private const val DEFAULT_BASE_URL: String = "https://service.miataru.com/v1/"
    }
}
