package com.android.sensors.data.local.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "RemoteKeys")
data class RemoteKeys(@NonNull @PrimaryKey val sensorId: String, val prevKey: Int?, val nextKey: Int?)