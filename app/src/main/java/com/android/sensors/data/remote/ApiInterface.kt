package com.android.sensors.data.remote

import com.android.sensors.data.remote.model.GetSensor
import com.android.sensors.data.remote.model.GetSensorsExtra
import com.android.sensors.data.remote.model.OperationStatus
import com.android.sensors.utils.calladapter.flow.Resource
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiInterface {

    @GET("/instrumentation/sensors")
    fun getSensors(): Flow<Resource<List<GetSensor>>>

    @GET("/instrumentation/sensors/extra")
    suspend fun getSensorsExtra(
        @Query("limit") limit: Int = 10,
        @Query("page") page: Int
    ): Response<GetSensorsExtra>

    @GET("/instrumentation/sensors/{sensorId}")
    fun getSensor(@Path("sensorId") sensorId: String): Flow<Resource<GetSensor>>

    @Multipart
    @POST("/instrumentation/sensors")
    fun createSensor(
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("source") source: RequestBody,
        @Part("moreInfo") moreInfo: RequestBody,
        @Part file: MultipartBody.Part?
    ): Flow<Resource<GetSensor>>

    @Multipart
    @PUT("/instrumentation/sensors/{sensorId}")
    fun updateSensor(
        @Path("sensorId") sensorId: String,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("source") source: RequestBody,
        @Part("moreInfo") moreInfo: RequestBody,
        @Part file: MultipartBody.Part?
    ): Flow<Resource<GetSensor>>

    @DELETE("/instrumentation/sensors/{sensorId}")
    fun deleteSensor(@Path("sensorId") sensorId: String): Flow<Resource<OperationStatus>>

}