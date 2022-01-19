package com.android.sensors.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OperationStatus(
    @Json(name = "operationResult")
    var operationResult: String = "",
    @Json(name = "operationName")
    val operationName: String = ""
)