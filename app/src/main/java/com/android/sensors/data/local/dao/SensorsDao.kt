package com.android.sensors.data.local.dao

import androidx.room.*
import com.android.sensors.data.local.model.SensorsDbModel

@Dao
interface SensorsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSensor(sensorsDbModel: SensorsDbModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSensors(sensorsDbModels: List<SensorsDbModel>)

    @Query("DELETE FROM SensorsDbModel WHERE id LIKE :sensorsId")
    fun deleteSensor(sensorsId: Long)

    @Query("DELETE FROM SensorsDbModel WHERE id IN (:sensorsIds)")
    fun deleteAllSensors(sensorsIds: List<Long>)

    @Delete
    fun deleteSensors(sensorsDbModels: List<SensorsDbModel>)

    @Query("SELECT * FROM SensorsDbModel ORDER BY id ASC")
    fun getAllSensors(): List<SensorsDbModel>
}