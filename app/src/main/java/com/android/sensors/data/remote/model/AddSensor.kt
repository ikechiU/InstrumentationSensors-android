package com.android.sensors.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AddSensor(
    @Json(name = "title")
    val title: String = "",
    @Json(name = "description")
    val description: String = "",
    @Json(name = "source")
    val source: String = "",
    @Json(name = "moreInfo")
    val moreInfo: String = "",
    @Json(name = "imageUrl")
    val imageUrl: String = ""
)