package com.android.sensors.data.remote.datasource

import com.android.sensors.data.remote.ApiInterface
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class RemoteDatasource @Inject constructor(val apiInterface: ApiInterface) {

    fun getSensors() = apiInterface.getSensors()

    fun createSensor(
        title: RequestBody,
        description: RequestBody,
        source: RequestBody,
        moreInfo: RequestBody,
        file: MultipartBody.Part?
    ) = apiInterface.createSensor(title, description, source, moreInfo, file)

    fun getSensor(sensorId: String) = apiInterface.getSensor(sensorId)

    fun updateSensor(
        sensorId: String,
        title: RequestBody,
        description: RequestBody,
        source: RequestBody,
        moreInfo: RequestBody,
        file: MultipartBody.Part?
    ) = apiInterface.updateSensor(sensorId, title, description, source, moreInfo, file)

    fun deleteSensor(sensorId: String) = apiInterface.deleteSensor(sensorId)
}