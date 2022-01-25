package com.android.sensors.data.local.model

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "SensorsDbModel")
data class SensorsDbModel(
    @NonNull @PrimaryKey val id: String = "",
    @ColumnInfo(name = "title") val title: String? = "",
    @ColumnInfo(name = "description") val description: String? = "",
    @ColumnInfo(name = "source") val source: String? = "",
    @ColumnInfo(name = "moreInfo") val moreInfo: String? = "",
    @ColumnInfo(name = "imageUrl") val imageUrl: String? = ""
)