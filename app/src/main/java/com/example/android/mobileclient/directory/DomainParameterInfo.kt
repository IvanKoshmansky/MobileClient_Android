package com.example.android.mobileclient.directory

import com.example.android.mobileclient.database.DatabaseParameterInfo

data class DomainParameterInfo constructor (
    val paramUniqueId: Long,        // уникальный идентификатор для room
    val devtypeId: Int,             // идентификация параметра на сервере
    val parametersListId: Int,      // идентификация параметра на сервере
    val parameterTypesId: Int,      // идентификация параметра на сервере
    val parameterTypeClassId: Int,  // идентификация параметра на сервере
    val paramName: String,          // имя параметра
    val mnemoCode: String,          // мнемокод
    val measUnit: String,           // единица измерения
    val paramPositions: Int,        // количество знаков до запятой
    val paramPrecision: Int,        // количество знаков после запятой
    var showOnTable: Boolean,       // показывать в таблице "данные"
    var showOnChart: Boolean        // показывать на графике
)
