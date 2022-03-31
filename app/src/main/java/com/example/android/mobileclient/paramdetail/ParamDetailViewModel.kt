package com.example.android.mobileclient.paramdetail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.android.mobileclient.directory.DirectoryManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import javax.inject.Inject

class ParamDetailViewModel @Inject constructor (
    application: Application,
    val directoryManager: DirectoryManager) : AndroidViewModel(application) {

    val parameterInfos = directoryManager.parameterInfos

    var viewModelJob = Job()

    val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
}
