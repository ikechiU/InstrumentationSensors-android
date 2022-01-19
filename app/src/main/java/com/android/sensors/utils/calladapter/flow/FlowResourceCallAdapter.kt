package com.android.sensors.utils.calladapter.flow

import com.android.sensors.utils.calladapter.flow.Resource.Error
import com.android.sensors.utils.calladapter.flow.Resource.Success
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.awaitResponse
import java.lang.reflect.Type

class FlowResourceCallAdapter<R>(
    private val responseType: Type,
    private val isSelfExceptionHandling: Boolean
) : CallAdapter<R, Flow<Resource<R>>> {

    override fun responseType() = responseType

    override fun adapt(call: Call<R>) = flow<Resource<R>> {

        // Firing loading resource
        emit(Resource.Loading<R>())

        val resp = call.awaitResponse()

        if (resp.isSuccessful) {
            resp.body()?.let { data ->
                // Success
                emit(Success(null, data))
            } ?: kotlin.run {
                // Error
                emit(Error("Response can't be null"))
            }
        } else {
            // Error
            val errorBody = resp.message()
            emit(Error(errorBody))
        }

    }.catch { error: Throwable ->
        if (isSelfExceptionHandling) {
            emit(Error(error.message ?: "Something went wrong"))
        } else {
            throw error
        }
    }
}