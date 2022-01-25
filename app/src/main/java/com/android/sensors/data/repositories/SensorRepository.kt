package com.android.sensors.data.repositories

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.android.sensors.data.local.AppDatabase
import com.android.sensors.data.local.datasource.LocalDatasource
import com.android.sensors.data.local.model.SensorsDbModel
import com.android.sensors.data.remote.ApiInterface
import com.android.sensors.data.remote.datasource.RemoteDatasource
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@ActivityRetainedScoped
class SensorRepository @Inject constructor(
    val remote: RemoteDatasource,
    val local: LocalDatasource,
    private val appDatabase: AppDatabase,
) {

    @OptIn(ExperimentalPagingApi::class)
    fun fetchSensors(): Flow<PagingData<SensorsDbModel>> {
        return Pager(
            PagingConfig(
                pageSize = 3, enablePlaceholders = false, prefetchDistance = 3
            ),
            remoteMediator = SensorsRemoteMediator(remote.apiInterface, appDatabase),
            pagingSourceFactory = { appDatabase.sensorsDao().getAllSensorsExtra() }
        ).flow
    }

}