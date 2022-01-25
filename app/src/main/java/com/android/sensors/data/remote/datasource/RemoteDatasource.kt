package com.android.sensors.data.remote.datasource

import com.android.sensors.data.remote.ApiInterface
import com.android.sensors.data.remote.model.AddSensor
import javax.inject.Inject

class RemoteDatasource @Inject constructor(val apiInterface: ApiInterface) {

    fun getSensors() = apiInterface.getSensors()
    fun addSensor(sensor: AddSensor) = apiInterface.addSensor(sensor)
    fun getSensor(sensorId: String) = apiInterface.getSensor(sensorId)
    fun updateSensor(sensorId: String, sensor: AddSensor) =
        apiInterface.updateSensor(sensorId, sensor)

    fun deleteSensor(sensorId: String) = apiInterface.deleteSensor(sensorId)
}