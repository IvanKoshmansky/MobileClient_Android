package com.example.android.mobileclient.directory

import android.app.AppComponentFactory
import com.example.android.mobileclient.storage.LocalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

//
// менеджер справочника параметров
// нужен для управления видимостью параметров в таблице или на графике (какие столбцы отображать)
// а также для подготовки запроса к серверу
//

@Singleton
class DirectoryManager @Inject constructor (
    private val localRepository: LocalRepository,
    private val directoryComponentFactory: DirectoryComponent.Factory
) {
    val directoryComponent = directoryComponentFactory.create()

    val parameterInfos = localRepository.parameterInfos

    // по данным о DevTypeId из репозитория запрашивает справочник параметров
    suspend fun refreshDirectory() {
        // работа с локальной БД только в потоке Dispatchers.IO
        withContext(Dispatchers.IO) {
            // получить информацию обо всех имеющихся DevTypeId
            // здесь написан отдельный облегченный запрос, поскольку обращаться к LiveData напрямую, без обсервера не получается
            val allDevTypeIds = localRepository.database.databaseDao.getAllDevTypeIds()

            // фильтрация множества с помощью функции filter():
            // для каждого элемента setOfDevTypeIds (it) проверяется содержится ли он в множестве supportedDevTypeIds
            val setOfDevTypeIds = allDevTypeIds.toSet() // множество всех DevTypeIds
            val supportedDevTypeIds = setOf(5, 9) // множество поддерживаемых DevTypeIds
            val filteredDevTypeIds = setOfDevTypeIds.filter { supportedDevTypeIds.contains(it) }

            localRepository.refreshParameterInfos(filteredDevTypeIds.toTypedArray())
        }
    }

    suspend fun setShowOnTable(domainParameterInfo: DomainParameterInfo, show: Boolean) {
        domainParameterInfo.showOnTable = show
        localRepository.updateParameterInfo(domainParameterInfo)
    }

    suspend fun setShowOnChart(domainParameterInfo: DomainParameterInfo, show: Boolean) {
        domainParameterInfo.showOnChart = show
        localRepository.updateParameterInfo(domainParameterInfo)
    }
}
