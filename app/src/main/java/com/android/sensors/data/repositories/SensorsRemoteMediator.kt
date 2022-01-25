package com.android.sensors.data.repositories

import androidx.paging.*
import androidx.room.withTransaction
import com.android.sensors.data.local.AppDatabase
import com.android.sensors.data.local.model.RemoteKeys
import com.android.sensors.data.local.model.SensorsDbModel
import com.android.sensors.data.remote.ApiInterface
import com.android.sensors.data.remote.model.GetSensor
import com.android.sensors.utils.Const.DEFAULT_PAGE_INDEX
import retrofit2.HttpException
import java.io.IOException
import java.io.InvalidObjectException
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class SensorsRemoteMediator @Inject constructor (
    private val apiInterface: ApiInterface,
    private val appDatabase: AppDatabase,
) : RemoteMediator<Int, SensorsDbModel>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, SensorsDbModel>
    ): MediatorResult {
        return try {

            val page = when (val loadKey = getKeyPageData(loadType, state)) {
                is MediatorResult.Success -> {
                    return loadKey
                }
                else -> {
                    loadKey as Int
                }
            }

            val response = apiInterface.getSensorsExtra(limit = state.config.pageSize, page = page)
            val listing = response.body()?.sensorsRests
            val isEndOfList = listing.isNullOrEmpty()

            if (listing != null) {

                appDatabase.withTransaction {

                    if(loadType == LoadType.REFRESH) {
                        appDatabase.remoteKeysDao().clearRemoteKeys()
                        appDatabase.sensorsDao().clearAllSensors()
                    }

                    val prevKey = if(page == DEFAULT_PAGE_INDEX) null else page - 1
                    val nextKey = if(isEndOfList) null else page + 1
                    val keys = listing.map {
                        RemoteKeys(sensorId = it.sensorId!!, prevKey, nextKey)
                    }

                    val sensors = mapSensorsDb(listing)
                    appDatabase.sensorsDao().insertAllSensors(sensors)
                    appDatabase.remoteKeysDao().insertAll(keys)
                }
            }

            MediatorResult.Success(endOfPaginationReached = isEndOfList)

        } catch (exception: IOException) {
            MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            MediatorResult.Error(exception)
        }

    }

    private suspend fun getKeyPageData(loadType: LoadType, state: PagingState<Int, SensorsDbModel>): Any? {
        return when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getClosestRemoteKey(state)
                remoteKeys?.nextKey?.minus(1) ?: DEFAULT_PAGE_INDEX
            }
            LoadType.PREPEND -> {
                val remoteKeys = getFirstRemoteKey(state)
                    ?: throw InvalidObjectException("Invalid state, key should not be null")
                //end of list condition reached
                remoteKeys.prevKey ?: return MediatorResult.Success(endOfPaginationReached = true)
                remoteKeys.prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getLastRemoteKey(state)
                    ?: throw InvalidObjectException("Remote key should not be null for $loadType")
                remoteKeys.nextKey
            }
        }
    }

    private suspend fun getClosestRemoteKey(state: PagingState<Int, SensorsDbModel>): RemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { sensorId ->
                appDatabase.remoteKeysDao().remoteKeysSensorsId(sensorId)
            }
        }
    }

    private suspend fun getFirstRemoteKey(state: PagingState<Int, SensorsDbModel>): RemoteKeys? {
        return state.pages
            .firstOrNull { it.data.isNotEmpty() }
            ?.data?.firstOrNull()
            ?.let { sensorsDbModel -> appDatabase.remoteKeysDao().remoteKeysSensorsId(sensorsDbModel.id) }
    }

    private suspend fun getLastRemoteKey(state: PagingState<Int, SensorsDbModel>): RemoteKeys? {
        return state.pages
            .lastOrNull { it.data.isNotEmpty() }
            ?.data?.lastOrNull()
            ?.let { sensorsDbModel -> appDatabase.remoteKeysDao().remoteKeysSensorsId(sensorsDbModel.id) }
    }

    private fun mapSensorsDb(getSensor: List<GetSensor>): List<SensorsDbModel> {
        return (getSensor).map {
            with(it) {
                SensorsDbModel(sensorId!!, title, description, source, moreInfo, imageUrl)
            }
        }
    }

}