package com.example.android.mobileclient.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.android.mobileclient.device.DeviceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

//
// использутся для предварительной очистки таблиц кэша
//

class MainViewModel @Inject constructor (
    application: Application,
    val deviceManager: DeviceManager
) : AndroidViewModel(application) {

    // This is the job for all coroutines started by this ViewModel.
    // Cancelling this job will cancel all coroutines started by this ViewModel.
    private val viewModelJob = SupervisorJob()

    //This is the main scope for all coroutines launched by MainViewModel.
    //Since we pass viewModelJob, you can cancel all coroutines launched by uiScope by calling
    //viewModelJob.cancel()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    fun cleanupTables() {
        viewModelScope.launch {
            deviceManager.cleanupDeviceData()
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}
