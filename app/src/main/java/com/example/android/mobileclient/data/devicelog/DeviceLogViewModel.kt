package com.example.android.mobileclient.data.devicelog

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.example.android.mobileclient.database.DeviceLogObject
import com.example.android.mobileclient.device.DeviceManager
import com.example.android.mobileclient.storage.LocalRepository
import com.example.android.mobileclient.utils.*
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject

const val PAGE_SIZE = 10
const val LOAD_COUNT = 10

class DeviceLogViewModel @Inject constructor (
    application: Application,
    val deviceManager: DeviceManager,
    val localRepository: LocalRepository) : AndroidViewModel(application) {

    //---------------------------------------------------------------------------------------------
    // список для обсервера
    //---------------------------------------------------------------------------------------------

    // здесь сделано напрямую из room без трансформации database->domain поскольку не получилось сделать Transformations.map() для PagedList
    val pagingConfig = PagedList.Config.Builder()
        .setEnablePlaceholders(false)
        .setPageSize(PAGE_SIZE)
        .setInitialLoadSizeHint(LOAD_COUNT)
        .build()
    val deviceLogDataSourceFactory: DataSource.Factory<Int, DeviceLogObject> =
        localRepository.database.databaseDao.deviceLogByIndex()
    val deviceLogList = LivePagedListBuilder(deviceLogDataSourceFactory, pagingConfig)
        .build()

    //---------------------------------------------------------------------------------------------
    // Job + Scope
    //---------------------------------------------------------------------------------------------

    private val viewModelJob = Job()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    //---------------------------------------------------------------------------------------------
    // Переменные для реализации внутренней логики экрана
    //---------------------------------------------------------------------------------------------

    var beginDate: SelDate? = null
    var beginTime: SelTime? = null
    var endDate: SelDate? = null
    var endTime: SelTime? = null

    //---------------------------------------------------------------------------------------------
    // Переменные LiveData
    //---------------------------------------------------------------------------------------------

    val beginDateTextView = MutableLiveData<String>()
    val beginTimeTextView = MutableLiveData<String>()
    val endDateTextView = MutableLiveData<String>()
    val endTimeTextView = MutableLiveData<String>()

    val triggerBeginDateDialog = MutableLiveData<Boolean?>()
    val triggerBeginTimeDialog = MutableLiveData<Boolean?>()
    val triggerEndDateDialog = MutableLiveData<Boolean?>()
    val triggerEndTimeDialog = MutableLiveData<Boolean?>()
    val triggerInvalidDateTimeDialog = MutableLiveData<Boolean?>()

    val triggerLoadingDialog = MutableLiveData<Int>()
    val triggerConnectionErrorMessage = MutableLiveData<Boolean>()

    //---------------------------------------------------------------------------------------------
    // Init block
    //---------------------------------------------------------------------------------------------

    init {
        if (deviceManager.retrievingDeviceLogBeginDate) {
            beginDate = localRepository.sharedPreferencesStorage.deviceLogBeginDate
        }
        if (deviceManager.retrievingDeviceLogBeginTime) {
            beginTime = localRepository.sharedPreferencesStorage.deviceLogBeginTime
        }
        if (deviceManager.retrievingDeviceLogEndDate) {
            endDate = localRepository.sharedPreferencesStorage.deviceLogEndDate
        }
        if (deviceManager.retrievingDeviceLogEndTime) {
            endTime = localRepository.sharedPreferencesStorage.deviceLogEndTime
        }
        beginDate?.let {
            beginDateTextView.value = String.format(Locale.ENGLISH, "%02d.%02d.%04d", it.day, it.month + 1, it.year)
        }
        beginTime?.let {
            beginTimeTextView.value = String.format(Locale.ENGLISH, "%02d:%02d", it.hour, it.minute)
        }
        endDate?.let {
            endDateTextView.value = String.format(Locale.ENGLISH, "%02d.%02d.%04d", it.day, it.month + 1, it.year)
        }
        endTime?.let {
            endTimeTextView.value = String.format(Locale.ENGLISH, "%02d:%02d", it.hour, it.minute)
        }
    }

    //---------------------------------------------------------------------------------------------
    // Функции для реализации внутренней логики экрана
    //---------------------------------------------------------------------------------------------

    fun setBeginDateFromDialog(year: Int, month: Int, dayOfMonth: Int) {
        beginDateTextView.value = String.format(Locale.ENGLISH, "%02d.%02d.%04d", dayOfMonth, month + 1, year)
        if (beginDate == null) {
            beginDate = SelDate(year, month, dayOfMonth)
        } else {
            beginDate?.year = year
            beginDate?.month = month
            beginDate?.day = dayOfMonth
        }
        localRepository.sharedPreferencesStorage.deviceLogBeginDate = beginDate
        deviceManager.retrievingDeviceLogBeginDate = true
    }

    fun setBeginTimeFromDialog(hourOfDay: Int, minute: Int) {
        beginTimeTextView.value = String.format(Locale.ENGLISH, "%02d:%02d", hourOfDay, minute)
        if (beginTime == null) {
            beginTime = SelTime(hourOfDay, minute)
        } else {
            beginTime?.hour = hourOfDay
            beginTime?.minute = minute
        }
        localRepository.sharedPreferencesStorage.deviceLogBeginTime = beginTime
        deviceManager.retrievingDeviceLogBeginTime = true
    }

    fun setEndDateFromDialog(year: Int, month: Int, dayOfMonth: Int) {
        endDateTextView.value = String.format(Locale.ENGLISH, "%02d.%02d.%04d", dayOfMonth, month + 1, year)
        if (endDate == null) {
            endDate = SelDate(year, month, dayOfMonth)
        } else {
            endDate?.year = year
            endDate?.month = month
            endDate?.day = dayOfMonth
        }
        localRepository.sharedPreferencesStorage.deviceLogEndDate = endDate
        deviceManager.retrievingDeviceLogEndDate = true
    }

    fun setEndTimeFromDialog(hourOfDay: Int, minute: Int) {
        endTimeTextView.value = String.format(Locale.ENGLISH, "%02d:%02d", hourOfDay, minute)
        if (endTime == null) {
            endTime = SelTime(hourOfDay, minute)
        } else {
            endTime?.hour = hourOfDay
            endTime?.minute = minute
        }
        localRepository.sharedPreferencesStorage.deviceLogEndTime = endTime
        deviceManager.retrievingDeviceLogEndTime = true
    }

    //---------------------------------------------------------------------------------------------
    // Обработчики событий
    //---------------------------------------------------------------------------------------------

    fun onBeginDateClick() {
        if (beginDate == null) {
            val cal = Calendar.getInstance()
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH)
            val day = cal.get(Calendar.DAY_OF_MONTH)
            beginDate = SelDate(year, month, day)
        }
        triggerBeginDateDialog.value = true
    }

    fun onBeginTimeClick() {
        if (beginTime == null) {
            val hour = 0
            val minute = 0
            beginTime = SelTime(hour, minute)
        }
        triggerBeginTimeDialog.value = true
    }

    fun onEndDateClick() {
        if (endDate == null) {
            val cal = Calendar.getInstance()
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH)
            val day = cal.get(Calendar.DAY_OF_MONTH)
            endDate = SelDate(year, month, day)
        }
        triggerEndDateDialog.value = true
    }

    fun onEndTimeClick() {
        if (endTime == null) {
            val hour = 0
            val minute = 0
            endTime = SelTime(hour, minute)
        }
        triggerEndTimeDialog.value = true
    }

    // основная функция логики данного экрана: загрузка журнала устройств с сервера:
    fun onLoadDeviceLogClick() {
        val objectId = deviceManager.currentObjectId
        objectId?.let { device ->
            // есть выбранное устройтсво
            if ((beginDate != null) && (beginTime != null) && (endDate != null) && (endTime != null)) {
                // дата-время выбрана правильно
                val timestamps = DateTimeUtils.dateTimeRangeToTimestampRange(beginDate!!.year, beginDate!!.month, beginDate!!.day, beginTime!!.hour, beginTime!!.minute,
                                                                             endDate!!.year, endDate!!.month, endDate!!.day, endTime!!.hour, endTime!!.minute)
                if (timestamps.end > timestamps.begin) {
                    triggerLoadingDialog.value = LOADING_DIALOG_SHOW

                    val handler = CoroutineExceptionHandler { _, exception ->
                        exception.printStackTrace()
                        if (exception.message.equals("error")) {
                            // показать сообщение "проверьте соединение с сетью" если исключение с сообщением "error"
                            triggerConnectionErrorMessage.value = true
                        }
                    }

                    currentJob = viewModelScope.launch(handler) {
                        localRepository.refreshDeviceLog(device, timestamps.begin, timestamps.end)
                        triggerLoadingDialog.postValue(LOADING_DIALOG_HIDE)
                    }
                } else {
                    triggerInvalidDateTimeDialog.value = true
                }
            } else {
                triggerInvalidDateTimeDialog.value = true
            }
        }
    }

    private var currentJob: Job? = null
    fun cancelLoading() {
        currentJob?.cancel()
    }

    //---------------------------------------------------------------------------------------------
    // onCleared()
    //---------------------------------------------------------------------------------------------

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}
