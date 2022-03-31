package com.example.android.mobileclient.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.android.mobileclient.device.DomainDeviceInfo
import com.example.android.mobileclient.settings.DomainSettings

//
// настройки
//

@Entity
data class DatabaseSettings constructor (
    @PrimaryKey
    val groupName: String,   // название этой группы настроек
    val serverName: String,  // имя сервера в правильном формате
    val dbName: String,      // имя бд сервера
    val userName: String,    // имя пользователя бд сервера
    val dbPassword: String   // пароль пользователя бд сервера
)

fun List<DatabaseSettings>.asDomainSettings(): List<DomainSettings> {
    return map {
        DomainSettings(
            groupName = it.groupName,
            serverName = it.serverName,
            dbName = it.dbName,
            userName = it.userName,
            dbPassword = it.dbPassword
        )
    }
}

//
// информация об объекте
//

@Entity(tableName = "device_info_table")
data class DatabaseDeviceInfo constructor (
    @PrimaryKey
    @ColumnInfo(name = "object_id")
    val objectId: Int,
    @ColumnInfo(name = "devtype_id")
    val devtypeId: Int,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "uid")
    val uid: String
) {
    fun asDomainDeviceInfo() = DomainDeviceInfo(objectId, devtypeId, name, uid)
}

fun List<DatabaseDeviceInfo>.asDomainDeviceInfo(): List<DomainDeviceInfo> {
    return map {
        DomainDeviceInfo(
            objectId = it.objectId,
            devtypeId = it.devtypeId,
            name = it.name,
            uid = it.name
        )
    }
}

/**
 * справочник параметров
 * ВАЖНОЕ ДОПУЩЕНИЕ: устройства и их параметры на сервере могут только добавляться, удаляться не могут
 */

@Entity(tableName = "parameter_info_table")
data class DatabaseParameterInfo constructor (
    @PrimaryKey
    @ColumnInfo(name = "param_unique_id")
    var paramUniqueId: Long,          // уникальный Id параметра для связки двух таблиц
    @ColumnInfo(name = "devtype_id")
    val devtypeId: Int,               // идентификация параметра на сервере
    @ColumnInfo(name = "parameters_list_id")
    val parametersListId: Int,        // идентификация параметра на сервере
    @ColumnInfo(name = "parameter_types_id")
    val parameterTypesId: Int,        // идентификация параметра на сервере
    @ColumnInfo(name = "parameter_type_class_id")
    val parameterTypeClassId: Int,    // идентификация параметра на сервере
    @ColumnInfo(name = "param_name")
    val paramName: String,            // свойства параметра
    @ColumnInfo(name = "mnemo_code")
    val mnemoCode: String,            // свойства параметра
    @ColumnInfo(name = "meas_unit")
    val measUnit: String,             // свойства параметра
    @ColumnInfo(name = "param_positions")
    val paramPositions: Int,          // свойства параметра
    @ColumnInfo(name = "param_precision")
    val paramPrecision: Int           // свойства параметра
)

// настройки отображения данных (в отдельной таблице)
@Entity(tableName = "parameter_info_show_table")
data class DatabaseParameterInfoShow constructor (
    @PrimaryKey
    @ColumnInfo(name = "_param_unique_id")
    var _paramUniqueId: Long,           // уникальный Id параметра для связки двух таблиц
    @ColumnInfo(name = "show_on_table")
    val showOnTable: Boolean = false,  // //true настройка отображения
    @ColumnInfo(name = "show_on_chart")
    val showOnChart: Boolean = false   // //true настройка отображения
)

// журнал событий устройств
// domain и database один объект, без преобразований "Transformations.map()"
@Entity(tableName = "device_log_table")
data class DeviceLogObject constructor (
    // _id с нуля с автоинкрементом
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var _id: Long = 0,
    @ColumnInfo(name = "date_time")
    val datetime: String,
    @ColumnInfo(name = "duration")
    val duration: Int,
    @ColumnInfo(name = "fw_version")
    val fwVersion: String,
    @ColumnInfo(name = "channel")
    val channel: Int,
    @ColumnInfo(name = "signal_level")
    val signalLevel: Int
)
