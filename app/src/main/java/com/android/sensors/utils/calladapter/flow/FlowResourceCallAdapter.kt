package com.android.sensors.utils.calladapter.flow

import com.google.gson.JsonParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.awaitResponse
import timber.log.Timber
import java.lang.reflect.Type

class FlowResourceCallAdapter<R>(
    private val responseType: Type,
    private val isSelfExceptionHandling: Boolean
) : CallAdapter<R, Flow<Resource<R>>> {

    override fun responseType() = responseType

    override fun adapt(call: Call<R>) = flow<Resource<R>> {

        val resp = call.awaitResponse()

        // Firing loading resource
        emit(Resource.Loading())

        if (resp.isSuccessful) {
            resp.body()?.let { data ->
                // Success
                emit(Resource.Success(null, data))
            } ?: kotlin.run {
                // Error
                emit(Resource.Error("Response can't be null"))
            }
        } else {
            // Error

            val msg = resp.errorBody()?.string()

            var errorBody: String

            if (msg.isNullOrEmpty()) {
                errorBody = resp.message()
            } else {
                try {
                    errorBody = JsonParser().parse(msg).asJsonObject["message"].asString
                } catch (e: Exception) {
                    errorBody = "Maximum upload size exceeded."
                    Timber.d(e.localizedMessage)
                }
            }

            emit(Resource.Error(errorBody))
        }

    }.catch { error: Throwable ->
        if (isSelfExceptionHandling) {
            emit(Resource.Error(error.message ?: "Something went wrong"))
        } else {
            throw error
        }
    }
}