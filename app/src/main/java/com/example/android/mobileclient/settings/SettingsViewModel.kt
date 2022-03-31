package com.example.android.mobileclient.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.mobileclient.database.getDatabase
import kotlinx.coroutines.*
import java.lang.IllegalArgumentException

class SettingsViewModel(application: Application) : AndroidViewModel (application) {

    private var viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val database = getDatabase(application)

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun onSaveSettingsClick() {
        viewModelScope.launch {

        }
    }

    fun onDeleteSettingsClick() {
        viewModelScope.launch {

        }
    }

    class Factory(val app:Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel(
                    app
                ) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}
