package com.android.sensors.data.local.datasource

import com.android.sensors.data.local.dao.SensorsDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDatasource @Inject constructor(
    private val sensorsDao: SensorsDao
)  {

    fun deleteSensor(sensorId: String) {
        sensorsDao.deleteSensor(sensorId)
    }
}