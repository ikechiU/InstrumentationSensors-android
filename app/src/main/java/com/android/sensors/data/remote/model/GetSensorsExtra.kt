package com.android.sensors.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GetSensorsExtra(
    @Json(name = "sensorsRests")
    val sensorsRests: List<GetSensor>,
    @Json(name = "totalPages")
    val totalPages: Int = 0
)