package com.example.android.mobileclient.overview

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.android.mobileclient.device.DeviceManager
import com.example.android.mobileclient.device.DomainDeviceInfo
import com.example.android.mobileclient.directory.DirectoryManager
import kotlinx.coroutines.*
import org.json.JSONException
import timber.log.Timber
import java.io.IOException
import java.lang.Exception
import java.sql.SQLException
import javax.inject.Inject

class OverviewViewModel @Inject constructor (
    application: Application,
    val deviceManager: DeviceManager,
    val directoryManager: DirectoryManager) : AndroidViewModel(application) {

    val deviceInfos = deviceManager.deviceInfos

    // This is the job for all coroutines started by this ViewModel.
    // Cancelling this job will cancel all coroutines started by this ViewModel.
    private val viewModelJob = SupervisorJob()

    //This is the main scope for all coroutines launched by MainViewModel.
    //Since we pass viewModelJob, you can cancel all coroutines launched by uiScope by calling
    //viewModelJob.cancel()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    fun setupView() {
        // работа по схеме viewModelScope.launch -> async (дочерняя, для возврата Deferred) -> serverRequest() -> прием Deferred
        // при возникновении исключения связанного с сетью, через связку async -> deferred
        // передается принудительно сгенерированное исключение IOException (генерируется в serverRequest())
        // "внутренние" исключения типа SQLException, JSONException, которые возникают и обрабатываются
        // в serverRequest() таким образом на уровень выше не передаются, нужно обязательно вызывать throw <новое исключение>
        // данная схема корретно не работает: падение приложения после прохода catch (e: IOException)
//        viewModelScope.launch {
//            val deferred = async {
//                deviceManager.refreshDeviceInfos()
//                directoryManager.refreshDirectory()
//            }
//            try {
//                deferred.await() // после запуска async поток разделяется: первая часть ждет deferred, вторая часть выполняет запрос
//            } catch (e: IOException) {
//                e.printStackTrace()
//                Timber.d("ошибка сети!")
//                // показать сообщение "ошибка сети"
//            }
//        }
        // корректная обработка исключений, сгенерированных через throw в корутинах делается через CoroutineExceptionHandler
        val handler = CoroutineExceptionHandler { _, exception ->
            println("CoroutineExceptionHandler got $exception")
            // выдать сообщение об ошибке сети:

        }
        viewModelScope.launch(handler) {
            deviceManager.refreshDeviceInfos()
            directoryManager.refreshDirectory()
        }
    }

    // переменные для навигации
    private val _navigateToDeviceDetail = MutableLiveData<Int>()
    val navigateToDeviceDetail
        get() = _navigateToDeviceDetail

    fun onDeviceClicked(id: Int) {
        _navigateToDeviceDetail.value = id
    }

    fun onDeviceDetailNavigated() {
        _navigateToDeviceDetail.value = null
    }
    // переменные для навигации

    // переменные для программного скроллинга
    private val _scrollToDevicePos = MutableLiveData<Int>()
    val scrollToDevicePos
        get() = _scrollToDevicePos

    private fun scrollingBegin(pos: Int) {
        _scrollToDevicePos.value = pos
    }

    fun setScrollingDone() {
        _scrollToDevicePos.value = null
    }
    // переменные для программного скроллинга

    // по вхождению подстроки в имя устройства запустить скроллинг списка
    fun triggerScrollingToDeviceName(substring: String) {
        viewModelScope.launch {
            val deviceInfo = deviceManager.getDeviceBySubstringInName(substring)
            deviceInfo?.let { info ->
                val currentList = deviceInfos.value
                // узнать какой индекс у данного объекта в списке (список был ранее передан адаптеру в submit list)
                currentList?.let {
                    // TODO: contains() в данном случае не работает?
                    for ((index, item) in it.toTypedArray().withIndex()) {
                        if (item.objectId == info.objectId) {
                            scrollingBegin(index)
                        }
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}
