package com.android.sensors.ui.viewmodel

import android.app.Application
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.android.sensors.data.repositories.SensorRepository
import com.android.sensors.data.local.model.SensorsDbModel
import com.android.sensors.data.remote.model.GetSensor
import com.android.sensors.data.remote.model.OperationStatus
import com.android.sensors.utils.calladapter.flow.Resource
import com.android.sensors.utils.livedata.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class SensorsViewModel @Inject constructor(
    private val sensorRepo: SensorRepository,
    application: Application
): AndroidViewModel(application) {

    //RemoteDatasource
    private val _shouldLoadSensors = SingleLiveEvent<Boolean>()
    val shouldLoadSensors:LiveData<Boolean> by  lazy { _shouldLoadSensors }

    fun startLoading() {
        _shouldLoadSensors.value = true
    }

    fun stopLoading() {
        _shouldLoadSensors.value = false
    }

    val getAllSensorsRemote: LiveData<Resource<List<GetSensor>>> = _shouldLoadSensors.switchMap {
        sensorRepo.remote.getSensors().asLiveData()
    }

    private val _getSensorRemote = MutableLiveData<String>()
    fun setSensorId(sensorId: String) {
        _getSensorRemote.value = sensorId
    }
    val getSensorRemote: LiveData<Resource<GetSensor>> = _getSensorRemote.switchMap {
        sensorRepo.remote.getSensor(it).asLiveData()
    }

    internal class CreateSensorFormData (
        val title: RequestBody,
        val description: RequestBody,
        val source: RequestBody,
        val moreInfo: RequestBody,
        val image: MultipartBody.Part?
    )

    private val _createSensor = MutableLiveData<CreateSensorFormData>()

    fun setCreateSensor(
        title: RequestBody,
        description: RequestBody,
        source: RequestBody,
        moreInfo: RequestBody,
        file: MultipartBody.Part?
    ) {
        _shouldLoadSensors.value = true
        _createSensor.value = CreateSensorFormData(title, description, source, moreInfo, file)
    }

    val createSensor: LiveData<Resource<GetSensor>> = _createSensor.switchMap {
        sensorRepo.remote.createSensor(
            it.title,
            it.description,
            it.source,
            it.moreInfo,
            it.image
        ).asLiveData()
    }


    internal class UpdateSensorFormData (
        val sensorId: String,
        val title: RequestBody,
        val description: RequestBody,
        val source: RequestBody,
        val moreInfo: RequestBody,
        val image: MultipartBody.Part?
    )

    private val _updateSensor = MutableLiveData<UpdateSensorFormData>()

    fun setUpdateSensor(
        sensorId: String,
        title: RequestBody,
        description: RequestBody,
        source: RequestBody,
        moreInfo: RequestBody,
        file: MultipartBody.Part?
    ) {
        _shouldLoadSensors.value = true
        _updateSensor.value = UpdateSensorFormData(sensorId, title, description, source, moreInfo, file)
    }

    val updateSensor: LiveData<Resource<GetSensor>> = _updateSensor.switchMap {
        sensorRepo.remote.updateSensor(
            it.sensorId,
            it.title,
            it.description,
            it.source,
            it.moreInfo,
            it.image
        ).asLiveData()
    }


    private val _deleteSensorRemote = MutableLiveData<String>()
    fun setDeleteSensor(sensorId: String) {
        _deleteSensorRemote.value = sensorId
    }
    val deleteSensorRemote: LiveData<Resource<OperationStatus>> = _deleteSensorRemote.switchMap {
        sensorRepo.remote.deleteSensor(it).asLiveData()
    }

    //RemoteMediator
    fun fetchSensors(): Flow<PagingData<SensorsDbModel>> {
        return sensorRepo.fetchSensors().cachedIn(viewModelScope)
    }

    //LocalDatasource
    fun deleteSensorLocal(id: String) {
        viewModelScope.launch(Dispatchers.Default) {
            sensorRepo.local.deleteSensor(id)
        }
    }

}