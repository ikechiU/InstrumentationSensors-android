package com.android.sensors.data.remote.remotemapper

import com.android.sensors.data.remote.model.GetSensor
import com.android.sensors.domain.SensorsModel

interface RemoteMapper {
    // GetSensor -> SensorsModel
    fun mapSensors(getSensor: List<GetSensor>): List<SensorsModel>

    fun mapSensor(getSensor: GetSensor): SensorsModel
}