package com.example.android.mobileclient.device

import android.app.AppComponentFactory
import com.example.android.mobileclient.storage.LocalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceManager @Inject constructor (
    private val localRepository: LocalRepository,
    private val deviceComponentFactory: DeviceComponent.Factory) {

    val deviceComponent = deviceComponentFactory.create()

    // параметры текущего выбранного устройства, при необходимости сделать get/set, private
    var currentObjectId: Int? = null // null ничего не выбрано
    var currentDeviceInfo: DomainDeviceInfo? = null

    // необходимо подгрузить интервавы времени для данных и журнала устройств из репозитория
    var retrievingDeviceDataBeginDate: Boolean = false
    var retrievingDeviceDataBeginTime: Boolean = false
    var retrievingDeviceDataEndDate: Boolean = false
    var retrievingDeviceDataEndTime: Boolean = false
    var retrievingDeviceLogBeginDate: Boolean = false
    var retrievingDeviceLogBeginTime: Boolean = false
    var retrievingDeviceLogEndDate: Boolean = false
    var retrievingDeviceLogEndTime: Boolean = false

    suspend fun fetchDeviceInfo() {
        currentObjectId?.let {
            withContext(Dispatchers.IO) {
                currentDeviceInfo = localRepository.database.databaseDao.getDeviceInfoById(currentObjectId!!).asDomainDeviceInfo()
            }
        }
    }

    val deviceInfos = localRepository.deviceInfos

    suspend fun refreshDeviceInfos() {
        localRepository.refreshDeviceInfos()
    }

    // по выделенным символам в имени объекта вернуть его позицию в списке (для адаптера)
    suspend fun getDeviceBySubstringInName(s: String): DomainDeviceInfo? {
        return localRepository.getDeviceBySubstringInName(s)
    }

    suspend fun cleanupDeviceData() {
        localRepository.deleteAllDeviceData()
        localRepository.deleteAllDeviceLog()
    }
}
