package com.android.sensors.utils.calladapter.flow

sealed class Resource<T> {

    class Loading<T> : Resource<T>()

    data class Success<T>(
        val message: String?,
        val data: T
    ) : Resource<T>()

    data class Error<T>(
        val errorData: String
    ) : Resource<T>()

}