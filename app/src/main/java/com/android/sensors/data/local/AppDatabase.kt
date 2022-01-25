package com.android.sensors.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.android.sensors.data.local.dao.RemoteKeysDao
import com.android.sensors.data.local.dao.SensorsDao
import com.android.sensors.data.local.model.RemoteKeys
import com.android.sensors.data.local.model.SensorsDbModel

@Database(entities = [SensorsDbModel::class, RemoteKeys::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun sensorsDao(): SensorsDao
    abstract fun remoteKeysDao(): RemoteKeysDao
}