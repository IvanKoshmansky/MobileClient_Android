package com.example.android.mobileclient.overview.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.android.mobileclient.device.DeviceManager
import com.example.android.mobileclient.device.DomainDeviceInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class DetailViewModel @Inject constructor (
    application: Application,
    val deviceManager: DeviceManager
) : AndroidViewModel(application) {

    private val viewModelJob = Job()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    // для стыка с UI использовать только LiveData
    val deviceInfo = MutableLiveData<DomainDeviceInfo>()

    // задать текущий objectId, подгрузить информацию об устройстве из репозитория и при необходимости очистить таблицы с данными и журнал
    fun setupDevice(newObjectId: Int) {
        val needToCleanupTables = deviceManager.currentObjectId != newObjectId // нужно ли почистить кэш
        deviceManager.currentObjectId = newObjectId
        viewModelScope.launch {
            deviceManager.fetchDeviceInfo() // по objectId загрузить из репозитория полную информацию об устройстве
            deviceInfo.value = deviceManager.currentDeviceInfo // обновить переменную LiveData для интерфейса
            if (needToCleanupTables) {
                deviceManager.cleanupDeviceData()
            }
        }
    }

    // переменные для навигации
    private val _navigateToDeviceData = MutableLiveData<Boolean?>()
    val navigateToDeviceData
        get() = _navigateToDeviceData

    fun onDataClicked() {
        _navigateToDeviceData.value = true
    }

    fun onDeviceDataNavigated() {
        _navigateToDeviceData.value = null
    }

    private val _navigateToDeviceLog = MutableLiveData<Boolean?>()
    val navigateToDeviceLog
        get() = _navigateToDeviceLog
    fun onDeviceLogClicked() {
        _navigateToDeviceLog.value = true
    }
    fun onDeviceLogNavigated() {
        _navigateToDeviceLog.value = null
    }
    // переменные для навигации

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}
