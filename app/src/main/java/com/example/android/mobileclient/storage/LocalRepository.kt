package com.example.android.mobileclient.storage

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.android.mobileclient.data.DataPagingObject
import com.example.android.mobileclient.database.*
import com.example.android.mobileclient.device.DomainDeviceInfo
import com.example.android.mobileclient.directory.DomainParameterInfo
import com.example.android.mobileclient.network.*
import com.example.android.mobileclient.settings.DomainSettings
import com.example.android.mobileclient.utils.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.sql.Timestamp
import java.sql.Types
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class LocalRepository @Inject constructor (val database: LocalDatabase, val sqliteDatabase: SQLiteDatabase, val sharedPreferencesStorage: SharedPreferencesStorage) {

    val settings: LiveData<List<DomainSettings>> = Transformations.map(database.databaseDao.getSettings()) {
        it.asDomainSettings()
    }

    val deviceInfos: LiveData<List<DomainDeviceInfo>> = Transformations.map(database.databaseDao.getDeviceInfos()) {
        it.asDomainDeviceInfo()
    }

    val parameterInfos: LiveData<List<DomainParameterInfo>> = Transformations.map(database.databaseDao.getArchiveParameterInfos()) {
        it.asDomainParameterInfo()
    }

    private fun updateParameterInfoInDatabase(domainParameterInfo: DomainParameterInfo) {
        val databaseParameterInfosShow = DatabaseParameterInfoShow (
            _paramUniqueId = domainParameterInfo.paramUniqueId,
            showOnTable = domainParameterInfo.showOnTable,
            showOnChart = domainParameterInfo.showOnChart
        )
        database.databaseDao.updateParameterInfoShow(databaseParameterInfosShow)
    }

    suspend fun updateParameterInfo(domainParameterInfo: DomainParameterInfo) {
        withContext(Dispatchers.IO) {
            updateParameterInfoInDatabase(domainParameterInfo)
        }
    }

    /**
     * ВАЖНОЕ ДОПУЩЕНИЕ: устройства и их параметры на сервере могут только добавляться, удаляться не могут
     */

    // получить информацию об устройствах на данном сервере и записать ее в таблицу device_info_table
    suspend fun refreshDeviceInfos() {
        // переключение в поток IO поскольку добавление в room работает только из него
        withContext(Dispatchers.IO) {
            val serverRequestInfo = ServerRequestInfo("jdbc:jtds:sqlserver://172.30.24.100:5000;databaseName=tctdb11;user=tctdb;password=tctpass;", arrayOf("SELECT ObjectID, DevTypeID, ObjName, UID FROM Objects"))

            // поскольку из корутины требуется возвращать более одного значения, то вместо пары async-deferred
            // используем каналы
            val channel = Channel<MutableList<NetworkDeviceInfo>>()
            launch {
                serverRequest(serverRequestInfo, channel) {
                    // параметр типа "лямда-выражение" можно вынести за фигурные скобки
                    // it - обращение к параметру л.в. (в данном случае JSONObject)
                    NetworkDeviceInfo(it.getInt("ObjectID"),
                                      it.getInt("DevTypeID"),
                                      it.getString("ObjName"),
                                      it.getString("UID"))
                }
            }
            //TODO: на будущее добавить фильтрацию не поддерживаемых типов объектов (таких как группа например или планшет)

            // для случая с возвратом нескольких контейнеров
            // перед вставкой в локальную БД нужно очистить всю таблицу и затем вставлять "с накатом"

            for (y in channel) {
                // MutableList<NetworkDeviceInfo> к Array<DatabaseDeviceInfo>
                val databaseDeviceInfos = y.asDatabaseModel()
                // Array<DatabaseDeviceInfo> к аргументам функции с переменным числом параметров
                database.databaseDao.insertAllDeviceInfos(*databaseDeviceInfos)
            }
        }
    }

    // получить инофрмацию о параметрах устройств и записать ее в таблицу parameter_info_table
    // результат запроса (таблица) используется затем в DirectoryManager для управления тем, какие данные отображать в таблице
    // а также для подготовки запроса к серверу
    // таблица parameter_info_table формируется в два этапа: запрос всех ParameterTypesID для данного DevTypeId
    // по ParameterTypesID получить таблицу с описанием параметров
    // если параметр не IsActive, то его в локальную БД не добавляем
    suspend fun refreshParameterInfos(arrayOfDevTypeIds: Array<Int>) {
        withContext(Dispatchers.IO) {

            // сформировать запрос для всех DevTypeId
            var queryString = "SELECT ParameterTypesID FROM ParameterTypes WHERE ((ParameterTypeClassID = $PARAMETER_TYPE_CLASS_ID_ARCHIVE) " +
                              "OR (ParameterTypeClassID = $PARAMETER_TYPE_CLASS_ID_READABLE)) AND ("
            for ((index, item) in arrayOfDevTypeIds.withIndex()) {
                if (index < arrayOfDevTypeIds.size - 1) {
                    queryString += "(DevTypeID = $item) OR "
                } else {
                    queryString += "(DevTypeID = $item))"
                }
            }

            var serverRequestInfo = ServerRequestInfo(
                "jdbc:jtds:sqlserver://172.30.24.100:5000;databaseName=tctdb11;user=tctdb;password=tctpass;",
                arrayOf(queryString)
            )

            queryString = "SELECT ParametersListID, ParameterTypesID, DevTypeID, ParameterTypeClassID, " +
                          "ParamName, MnemoCode, MeasUnit, ModuleID, ModuleNum, ParamPositions, ParamPrecision, " +
                          "IsActive FROM ParametersList WHERE (IsActive = 1) AND ("

            val channelForParametersListID = Channel<MutableList<Int>>()
            launch {
                serverRequest(serverRequestInfo, channelForParametersListID) {
                    it.getInt("ParameterTypesID") // Int <- JSON Object
                }
            }

            for (y in channelForParametersListID) {
                // сформировать следущий запрос
                // способ перебора элементов массива с индексами и элементами одновременно
                // с помощью функции withIndex()
                for ((index, item) in y.toIntArray().withIndex()) {
                    if (index < y.size - 1) {
                        queryString += "(ParameterTypesID = $item) OR "
                    } else {
                        queryString += "(ParameterTypesID = $item))"
                    }
                }
            }

            serverRequestInfo = ServerRequestInfo(
                "jdbc:jtds:sqlserver://172.30.24.100:5000;databaseName=tctdb11;user=tctdb;password=tctpass;",
                arrayOf(queryString)
            )

            // запрос сформирован, отправляем
            val channel = Channel<MutableList<NetworkParameterInfo>>()
            launch {
                serverRequest(serverRequestInfo, channel) {
                    NetworkParameterInfo(
                        devtypeId            = it.getInt("DevTypeID"),
                        parametersListId     = it.getInt("ParametersListID"),
                        parameterTypesId     = it.getInt("ParameterTypesID"),
                        parameterTypeClassId = it.getInt("ParameterTypeClassID"),
                        paramName            = it.getString("ParamName"),
                        mnemoCode            = it.getString("MnemoCode"),
                        measUnit             = it.getString("MeasUnit"),
                        paramPositios        = it.getString("ParamPositions"),
                        paramPrecision       = it.getString("ParamPrecision")
                    )
                }
            }
            for (y in channel) {
                val databaseParameterInfos = y.asDatabaseModel()
                database.databaseDao.insertAllParameterInfos(databaseParameterInfos)
            }
        }
    }

    suspend fun refreshArchiveData(objectId: Int, beginTime: Timestamp, endTime: Timestamp) {
        withContext(Dispatchers.IO) {

            // узнать какие параметры выбраны для отображения
            val selectedParameters = database.databaseDao.getSelectedArchiveParameterInfosForData()

            // обновить данные в SharedPreferencesStorage (для корректного отображения заголовка и разбора таблицы)
            sharedPreferencesStorage.refreshDataTableInfo(selectedParameters)

            // подготовить таблицу для хранения данных (количество столбцов заранее неизвестно)
            sqliteDatabase.prepareDataTable()

            // подготовить запрос к серверу
            val storedProcCalls = mutableListOf<StoredProcCall>()
            val period = 3 // TODO: заменить харкод на настроечное значение (в сутках)
            val add = period * 24 * 60 * 60 * 1000
            // TimeUnit.DAYS.toMillis(3)
            var intervalBegin: Timestamp
            var intervalEnd = Timestamp(beginTime.getTime())

            do {
                intervalBegin = Timestamp(intervalEnd.getTime())
                intervalEnd = Timestamp(intervalEnd.getTime() + add)
                if (intervalEnd.getTime() > endTime.getTime()) {
                    intervalEnd = Timestamp(endTime.getTime())
                }
                val storedProcCall = StoredProcCall("Get_Data_Alm",
                    arrayOf(StoredProcParams(Types.INTEGER, null, selectedParameters[0].parameterTypesId, null),
                            StoredProcParams(Types.INTEGER, null, objectId, null),
                            StoredProcParams(Types.TIMESTAMP, null, null, intervalBegin),
                            StoredProcParams(Types.TIMESTAMP, null, null, intervalEnd)))
                storedProcCalls.add(storedProcCall)
            } while (intervalEnd.getTime() < endTime.getTime())

            val serverRequestInfo = ServerRequestInfo(
                "jdbc:jtds:sqlserver://172.30.24.100:5000;databaseName=tctdb11;user=tctdb;password=tctpass;",
                storedProcCalls.toTypedArray())

            val channel = Channel<MutableList<NetworkDataResponce>>()

            launch {
                serverRequest(serverRequestInfo, channel) {
                    NetworkDataResponce(
                        parametersListId = it.getInt("ParametersListID"),  // сырые данные
                        valueDateTime = it.getString("ValueDateTime"),     // никаких преобразований не делаем
                        paramValueTXT = it.getString("ParamValueTXT"),
                        reason = it.getString("Reason")
                    )
                }
            }
            for (y in channel) {
                sqliteDatabase.addDataToTable(y)
            }
        }
    }

    /**
     * получить данные в таблицу с данными из репозитория
     */

    suspend fun getDataTableRowsCount(): Int {
        // пример возврата значения из БД в основной поток при переключении диспетчера
        return withContext(Dispatchers.IO) {
            val count = sqliteDatabase.getDataTableRowsCount()
            count
        }
    }

    suspend fun getDataTableRows(startPosition: Int, loadSize: Int): List<DataPagingObject> {
        return withContext(Dispatchers.IO) {
            val rows = sqliteDatabase.getDataTableRows(startPosition, loadSize).asDomainModel()
            rows
        }
    }

    // вернуть из БД позицию в списке для устройства с вхождением строки s в имя или null если ничего не найдено
    suspend fun getDeviceBySubstringInName(s: String): DomainDeviceInfo? {
        return withContext(Dispatchers.IO) {
            var deviceInfo: DomainDeviceInfo? = null
            val s1 = "%$s%"
            val tmp = database.databaseDao.getDeviceBySubstringInName(s1)
            tmp?.let {
                deviceInfo = it.asDomainDeviceInfo()
            }
            deviceInfo
        }
    }

    // удалить все данные из таблицы device_data_table
    suspend fun deleteAllDeviceData() {
        withContext(Dispatchers.IO) {
            sqliteDatabase.deleteAllDeviceData()
        }
    }

    //---------------------------------------------------------------------------------------------
    // обновление журнала устройств
    //---------------------------------------------------------------------------------------------

    // массив промежуточных объектов которые приходят с сервера (каждый объект создатся по JSON прототипу)
    private var readableParamsMap: Map<Int, String>? = null

    // получить карту (Map) вида <ParametersListID,ParamName> по информации в локальной БД
    private fun getReadbleParamsMap(devtypeId: Int): Map<Int, String> {
        val params = database.databaseDao.getReadableParams(devtypeId)
        val result = mutableMapOf<Int, String>()
        params.forEach {
            result.put(it.parametersListId, it.paramName)
        }
        return result
    }

    // функция упаковки промежуточных JSON объектов от сервера во внутренний формат room
    private fun packDeviceLogResponce(listOfResponces: List<NetworkDeviceLogResponce>, currentList: MutableList<NetworkDeviceLogObject>): MutableList<NetworkDeviceLogObject> {
        readableParamsMap?.let { paramsMap ->
            if (paramsMap.isNotEmpty()) {
                // карта параметров подготовлена
                val assembly = NetworkDeviceLogObject("", null, null, null, null)
                listOfResponces.forEach {
                    // цикл по объектам от сервера
                    if (it.isNotEmpty()) {
                        if (!it.sessionBeginDateTime.equals(assembly.datetime)) {
                            // новая дата время в списке - инициализируем сборщик
                            assembly.datetime = it.sessionBeginDateTime
                            assembly.duration = it.sessionDuration
                            assembly.fwVersion = null
                            assembly.channel = null
                            assembly.signalLevel = null
                        }
                        // разбор по полям
                        assembly.concatNetworkDeviceLogResponce(it, paramsMap)
                        if (assembly.isNotEpty()) {
                            // все поля заполнены, можно формировать новый объект и догружать его в список
                            val nextObject = NetworkDeviceLogObject(
                                assembly.datetime,
                                assembly.duration!!,
                                assembly.fwVersion!!,
                                assembly.channel!!,
                                assembly.signalLevel!!
                            )
                            currentList.add(nextObject)
                        }
                    }
                }
            }
        }
        return currentList
    }

    // загрузить с сервера журнал устройства за требуемые даты
    suspend fun refreshDeviceLog(objectId: Int, beginTime: Timestamp, endTime: Timestamp) {
        withContext(Dispatchers.IO) {
            // перейти в поток IO для работы с сервером и room

            // по objectId получить его devtypeId
            val devtypeId = database.databaseDao.getDeviceInfoById(objectId).devtypeId

            // получить карту Readble параметров в нужном формате (для разбора ответа с сервера)
            readableParamsMap = getReadbleParamsMap(devtypeId)
            readableParamsMap?.let { paramsMap ->
                if (paramsMap.isNotEmpty()) {
                    // в локальной БД есть информация о Readble параметрах для данного objectId

                    // подготовить запрос к серверу
                    val period = 3 // TODO: заменить харкод на настроечное значение (в сутках)
                    val add = TimeUnit.DAYS.toMillis(period.toLong())
                    var intervalBegin: Timestamp
                    var intervalEnd = Timestamp(beginTime.getTime())
                    var beginDateTime: String // в формате MS SQL Server
                    var endDateTime: String
                    var queryString: String
                    val queryStrings = mutableListOf<String>()

                    // разбивка на интервалы по три дня
                    do {
                        intervalBegin = Timestamp(intervalEnd.getTime())
                        intervalEnd = Timestamp(intervalEnd.getTime() + add)
                        if (intervalEnd.getTime() > endTime.getTime()) {
                            intervalEnd = Timestamp(endTime.getTime())
                        }

                        // преобразование Timestamp в формат MS SQL Server
                        beginDateTime = DateTimeUtils.dateTimeToMsSqlServerFormat(intervalBegin)
                        endDateTime = DateTimeUtils.dateTimeToMsSqlServerFormat(intervalEnd)

                        queryString = "SELECT ReadableParamValues.SessionID, ReadableParamValues.ParametersListID, ReadableParamValues.ParamValueTXT, " +
                                "selected_sessions.SessionBeginDateTime, selected_sessions.SessionDuration FROM ReadableParamValues " +
                                "JOIN (SELECT Sessions.SessionID, Sessions.SessionBeginDateTime, Sessions.SessionDuration FROM Sessions " +
                                "WHERE (ObjectID = $objectId" +
                                ") AND (SessionBeginDateTime >= " + beginDateTime +
                                ") AND (SessionBeginDateTime <= " + endDateTime + ")) AS selected_sessions " +
                                "ON (ReadableParamValues.SessionID = selected_sessions.SessionID) AND (ReadableParamValues.ParameterTypeClassID = $PARAMETER_TYPE_CLASS_ID_READABLE)"

                        // добавить строку подзапроса в список
                        queryStrings.add(queryString)
                    } while (intervalEnd.getTime() < endTime.getTime())

                    if (queryStrings.isNotEmpty()) {
                        // список объектов в формате room
                        var deviceLogObjects = mutableListOf<NetworkDeviceLogObject>()

                        // сформировать объект запроса
                        val serverRequestInfo = ServerRequestInfo(
                            "jdbc:jtds:sqlserver://172.30.24.100:5000;databaseName=tctdb11;user=tctdb;password=tctpass;",
                            queryStrings.toTypedArray())

                        // подготовить канал для приема ответов
                        val channel = Channel<MutableList<NetworkDeviceLogResponce>>()

                        launch {
                            serverRequest(serverRequestInfo, channel) {
                                NetworkDeviceLogResponce.Factory.create(it, paramsMap)
                            }
                        }

                        // преобразвание данных от сервера во внутренний формат
                        for (y in channel) {
                            deviceLogObjects = packDeviceLogResponce(y, deviceLogObjects)
                        }

                        // удалить старую информацию из таблицы device_log_table
                        database.databaseDao.deleteDeviceLog()

                        // сложить в локальную базу данных (network->database->(*vararg))
                        database.databaseDao.insertDeviceLog(*deviceLogObjects.asDatabaseModel())
                    }
                }
            }
        }
    }

    // удалить данные из таблицы device_log_table
    suspend fun deleteAllDeviceLog() {
        withContext(Dispatchers.IO) {
            database.databaseDao.deleteDeviceLog()
        }
    }
}
