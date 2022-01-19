package com.android.sensors.data.remote.datasource

import com.android.sensors.data.remote.ApiInterface
import com.android.sensors.data.remote.model.AddSensor
import com.android.sensors.data.remote.model.GetSensor
import com.android.sensors.data.remote.remotemapper.RemoteMapper
import com.android.sensors.domain.SensorsModel
import com.android.sensors.utils.Const.NEW_ID
import javax.inject.Inject
import javax.inject.Singleton

class RemoteDatasource @Inject constructor(private val apiInterface: ApiInterface) : RemoteMapper {

    fun getSensors() = apiInterface.getSensors()
    fun addSensor(sensor: AddSensor) = apiInterface.addSensor(sensor)
    fun getSensor(sensorId: String) = apiInterface.getSensor(sensorId)
    fun updateSensor(sensorId: String, sensor: AddSensor) =
        apiInterface.updateSensor(sensorId, sensor)

    fun deleteSensor(sensorId: String) = apiInterface.deleteSensor(sensorId)

    override fun mapSensors(getSensor: List<GetSensor>): List<SensorsModel> {
        return (getSensor).map { mapSensor(it) }
    }

    override fun mapSensor(getSensor: GetSensor): SensorsModel {
        return with(getSensor) {
            SensorsModel(NEW_ID, title, description, source, moreInfo, imageUrl)
        }
    }
}