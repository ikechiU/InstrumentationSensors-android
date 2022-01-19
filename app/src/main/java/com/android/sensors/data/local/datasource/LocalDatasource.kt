package com.android.sensors.data.local.datasource

import androidx.lifecycle.LiveData
import com.android.sensors.domain.SensorsModel

interface LocalDatasource {

    fun insertSensor(sensorsModel: SensorsModel)

    fun insertSensors(sensorsModel: List<SensorsModel>)

    fun deleteAllSensors(sensorIds: List<Long>)

    fun deleteSensors(sensorsModel: List<SensorsModel>)

    fun deleteSensor(sensorId: Long)

    fun getAllSensors(): LiveData<List<SensorsModel>>
}