package com.android.sensors.ui.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.android.sensors.data.SensorRepository
import com.android.sensors.data.remote.model.AddSensor
import com.android.sensors.data.remote.model.GetSensor
import com.android.sensors.data.remote.model.OperationStatus
import com.android.sensors.domain.SensorsModel
import com.android.sensors.ui.livedata.SingleLiveEvent
import com.android.sensors.utils.calladapter.flow.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    //LocalDatasource
    val getAllSensorsLocal: LiveData<List<SensorsModel>> = sensorRepo.local.getAllSensors()

    fun insertSensorLocal(getSensor: GetSensor) {
        viewModelScope.launch(Dispatchers.Default) {
            val sensor = sensorRepo.remote.mapSensor(getSensor)
            sensorRepo.local.insertSensor(sensor)
        }
    }

    fun insertSensorsLocal(getSensors: List<GetSensor>) {
        viewModelScope.launch(Dispatchers.Default) {
            val sensors = sensorRepo.remote.mapSensors(getSensors)
            sensorRepo.local.insertSensors(sensors)
        }
    }

    fun deleteSensorLocal(id: Long) {
        viewModelScope.launch(Dispatchers.Default) {
            sensorRepo.local.deleteSensor(id)
        }
    }

    fun deleteSensorsLocal(sensors: List<SensorsModel>) {
        viewModelScope.launch(Dispatchers.Default) {
            sensorRepo.local.deleteSensors(sensors)
        }
    }

    fun deleteAllSensorsLocal(ids: List<Long>) {
        viewModelScope.launch(Dispatchers.Default) {
            sensorRepo.local.deleteAllSensors(ids)
        }
    }

}