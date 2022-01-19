package com.android.sensors.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.android.sensors.data.local.dao.SensorsDao
import com.android.sensors.data.local.model.SensorsDbModel

@Database(entities = [SensorsDbModel::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun sensorsDao(): SensorsDao
}