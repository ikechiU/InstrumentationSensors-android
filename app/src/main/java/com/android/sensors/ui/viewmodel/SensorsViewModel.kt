package com.android.sensors.ui.viewmodel

import android.app.Application
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.android.sensors.data.repositories.SensorRepository
import com.android.sensors.data.local.model.SensorsDbModel
import com.android.sensors.data.remote.model.AddSensor
import com.android.sensors.data.remote.model.GetSensor
import com.android.sensors.data.remote.model.OperationStatus
import com.android.sensors.domain.SensorsModel
import com.android.sensors.utils.calladapter.flow.Resource
import com.android.sensors.utils.livedata.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SensorsViewModel @Inject constructor(
    private val sensorRepo: SensorRepository,
    application: Application
): AndroidViewModel(application) {

    //RemoteDatasource
    private val _shouldGetSensorsRemote = SingleLiveEvent<Boolean>()
    val getAllSensorsRemote: LiveData<Resource<List<GetSensor>>> = _shouldGetSensorsRemote.switchMap {
        sensorRepo.remote.getSensors().asLiveData()
    }

    init {
        _shouldGetSensorsRemote.value = true
    }

    private val _getSensorRemote = MutableLiveData<String>()
    fun setSensorId(sensorId: String) {
        _getSensorRemote.value = sensorId
    }
    val getSensorRemote: LiveData<Resource<GetSensor>> = _getSensorRemote.switchMap {
        sensorRepo.remote.getSensor(it).asLiveData()
    }

    private val _addSensorRemote = MutableLiveData<AddSensor>()
    fun setAddSensor(addSensor: AddSensor) {
        _addSensorRemote.value = addSensor
    }
    val addSensorRemote: LiveData<Resource<GetSensor>> = _addSensorRemote.switchMap {
        sensorRepo.remote.addSensor(it).asLiveData()
    }

    internal class Couple (val sensorId: String, val addSensor: AddSensor)
    private val _updateSensorRemote = MutableLiveData<Couple>()
    fun setUpdateSensor(sensorId: String, addSensor: AddSensor) {
        _updateSensorRemote.value = Couple(sensorId, addSensor)
    }
    val updateSensorRemote: LiveData<Resource<GetSensor>> = _updateSensorRemote.switchMap {
        sensorRepo.remote.updateSensor(it.sensorId, it.addSensor).asLiveData()
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