package com.android.sensors.data.remote

import com.android.sensors.data.remote.model.AddSensor
import com.android.sensors.data.remote.model.GetSensor
import com.android.sensors.data.remote.model.OperationStatus
import com.android.sensors.utils.calladapter.flow.Resource
import kotlinx.coroutines.flow.Flow
import retrofit2.http.*

interface ApiInterface {

    @GET("/instrumentation/sensors")
    fun getSensors(): Flow<Resource<List<GetSensor>>>

    @POST("/instrumentation/sensors")
    fun addSensor(@Body sensor: AddSensor): Flow<Resource<GetSensor>>

    @GET("/instrumentation/sensors/{sensorId}")
    fun getSensor(@Path("sensorId") sensorId: String): Flow<Resource<GetSensor>>

    @PUT("/instrumentation/sensors/{sensorId}")
    fun updateSensor(@Path("sensorId") sensorId: String, @Body sensor: AddSensor): Flow<Resource<GetSensor>>

    @DELETE("/instrumentation/sensors/{sensorId}")
    fun deleteSensor(@Path("sensorId") sensorId: String): Flow<Resource<OperationStatus>>

}