package com.android.sensors.data.local.dbmapper

import com.android.sensors.data.local.model.SensorsDbModel
import com.android.sensors.domain.SensorsModel

interface DbMapper {
    // SensorsDbModel -> SensorsModel
    fun mapSensors(sensorsDbModel: List<SensorsDbModel>): List<SensorsModel>
    fun mapSensor(sensorsDbModel: SensorsDbModel): SensorsModel

    //SensorsModel -> SensorsDbModel
    fun mapDbSensors(sensorsModel: List<SensorsModel>): List<SensorsDbModel>
    fun mapDbSensor(sensorsModel: SensorsModel): SensorsDbModel
}