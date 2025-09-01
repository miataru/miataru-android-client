package com.miataru.client.network

import com.miataru.client.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface MiataruService {
    @Headers("Content-Type: application/json")
    @POST("UpdateLocation")
    suspend fun updateLocation(@Body body: MiataruUpdateLocationRequest): Ack

    @Headers("Content-Type: application/json")
    @POST("GetLocation")
    suspend fun getLocation(@Body body: MiataruGetLocationRequest): MiataruGetLocationResponse

    @GET("GetLocationGeoJSON/{deviceID}")
    suspend fun getLocationGeoJSON(@Path("deviceID") deviceId: String): MiataruGetLocationGeoJSONResponse

    @Headers("Content-Type: application/json")
    @POST("GetLocationHistory")
    suspend fun getLocationHistory(@Body body: MiataruGetLocationHistoryRequest): MiataruGetLocationHistoryResponse

    @Headers("Content-Type: application/json")
    @POST("GetVisitorHistory")
    suspend fun getVisitorHistory(@Body body: MiataruGetVisitorHistoryRequest): MiataruGetVisitorHistoryResponse
}
