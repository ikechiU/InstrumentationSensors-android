package com.android.sensors.data.local.datasource

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.sensors.data.local.dao.SensorsDao
import com.android.sensors.data.local.dbmapper.DbMapper
import com.android.sensors.data.local.model.SensorsDbModel
import com.android.sensors.domain.SensorsModel
import com.android.sensors.utils.Const.NEW_ID
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDatasourceImpl @Inject constructor(
    private val sensorsDao: SensorsDao,
    private val dbMapper: DbMapper
) : LocalDatasource {

    private val _getAllSensors: MutableLiveData<List<SensorsModel>> by lazy {
        MutableLiveData<List<SensorsModel>>()
    }

    init {
        initDatabase(this::updateSensorsLiveData)
    }

    private fun initDatabase(postInitAction: () -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            postInitAction.invoke()
        }
    }

    override fun getAllSensors(): LiveData<List<SensorsModel>> = _getAllSensors

    private fun getAllSensorsSync(): List<SensorsModel>{
        val dbModel: List<SensorsDbModel> = sensorsDao.getAllSensors()
        return dbMapper.mapSensors(dbModel)
    }

    private fun updateSensorsLiveData() {
        _getAllSensors.postValue(getAllSensorsSync())
    }

    override fun insertSensor(sensorsModel: SensorsModel) {
        sensorsDao.insertSensor(dbMapper.mapDbSensor(sensorsModel))
        updateSensorsLiveData()
    }

    override fun insertSensors(sensorsModel: List<SensorsModel>) {
        sensorsDao.insertSensors(dbMapper.mapDbSensors(sensorsModel))
        updateSensorsLiveData()
    }

    override fun deleteAllSensors(sensorIds: List<Long>) {
       sensorsDao.deleteAllSensors(sensorIds)
        updateSensorsLiveData()
    }

    override fun deleteSensors(sensorsModel: List<SensorsModel>) {
        sensorsDao.deleteSensors(dbMapper.mapDbSensors(sensorsModel))
        updateSensorsLiveData()
    }

    override fun deleteSensor(sensorId: Long) {
        sensorsDao.deleteSensor(sensorId)
        updateSensorsLiveData()
    }

}