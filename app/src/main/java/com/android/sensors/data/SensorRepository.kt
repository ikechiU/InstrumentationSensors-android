package com.android.sensors.data

import com.android.sensors.data.local.datasource.LocalDatasource
import com.android.sensors.data.local.datasource.LocalDatasourceImpl
import com.android.sensors.data.remote.datasource.RemoteDatasource
import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject

@ActivityRetainedScoped
class SensorRepository @Inject constructor(
    remoteDatasource: RemoteDatasource,
    localDatasource: LocalDatasourceImpl
) {
    val remote = remoteDatasource
    val local = localDatasource
}