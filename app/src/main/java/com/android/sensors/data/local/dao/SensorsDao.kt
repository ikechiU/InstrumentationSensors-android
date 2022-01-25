package com.android.sensors.data.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.android.sensors.data.local.model.SensorsDbModel
import com.android.sensors.domain.SensorsModel

@Dao
interface SensorsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllSensors(sensorsDbModels: List<SensorsDbModel>)

    @Query("DELETE FROM SensorsDbModel")
    fun clearAllSensors()

    @Query("SELECT * FROM SensorsDbModel")
    fun getAllSensorsExtra(): PagingSource<Int, SensorsDbModel>

    @Query("DELETE FROM SensorsDbModel WHERE id LIKE :sensorsId")
    fun deleteSensor(sensorsId: String)
}