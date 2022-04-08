package com.android.sensors.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GetSensorsExtra(
    @Json(name = "sensorsRests")
    val sensorsRests: List<GetSensor>,
    @Json(name = "pageListCount")
    val pageListCount: Int = 0,
    @Json(name = "currentPage")
    val currentPage: Int = 0,
    @Json(name = "previousPage")
    val previousPage: String? = null,
    @Json(name = "nextPage")
    val nextPage: String? = null,
    @Json(name = "totalListCount")
    val totalListCount: Long = 0L,
    @Json(name = "totalPages")
    val totalPages: Int = 0
)