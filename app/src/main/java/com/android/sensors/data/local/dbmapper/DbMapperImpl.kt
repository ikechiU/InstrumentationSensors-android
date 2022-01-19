package com.android.sensors.data.local.dbmapper

import com.android.sensors.data.local.model.SensorsDbModel
import com.android.sensors.domain.SensorsModel
import com.android.sensors.utils.Const.NEW_ID

class DbMapperImpl: DbMapper{
    override fun mapSensors(sensorsDbModel: List<SensorsDbModel>): List<SensorsModel> {
        return (sensorsDbModel).map { mapSensor(it) }
    }

    override fun mapSensor(sensorsDbModel: SensorsDbModel): SensorsModel {
        return with (sensorsDbModel) {
            SensorsModel(id, title, description, source, moreInfo, imageUrl)
        }
    }

    override fun mapDbSensors(sensorsModel: List<SensorsModel>): List<SensorsDbModel> {
        return sensorsModel.map { mapDbSensor(it) }
    }

    override fun mapDbSensor(sensorsModel: SensorsModel): SensorsDbModel {
        return with(sensorsModel){
            if (id == NEW_ID) {
                SensorsDbModel(title = title, description = description, source = source, moreInfo = moreInfo, imageUrl = imageUrl)
            } else {
                SensorsDbModel(id, title, description, source, moreInfo, imageUrl)
            }
        }
    }
}