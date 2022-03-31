package com.example.android.mobileclient.storage

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.example.android.mobileclient.database.*
import com.example.android.mobileclient.utils.*

//
// хранение временной информации небольшого объема
//

//в самой таблице названия колонок следующие:
//ID (нужно для адаптера), дата-время, тревожность, "ParametersListID нулевого параметра", "ParametersListID первого переметра" итд
//0                        2021010101  0            1001                                   1002

// информация о столбце
data class DataTableColumnInfo (
    val visibleColumnName: String,  // отображаемое наименование столбца
    val paramPositions: Int,        // кол-во знаков до запятой
    val paramPrecision: Int         // кол-во знаков после запятой
)

// информация о таблице в целом
// здесь в качестве ключа используется ParametersLisId
data class DataTableInfo (
    val info: MutableMap<Int, DataTableColumnInfo>
)

class SharedPreferencesStorage (val context: Context) {

    private val FILE_NAME = "DATA_TABLE_DESC"
    private val KEY_PARAMS_COUNT = "PARAMS_COUNT"

    val dataTableInfo: DataTableInfo?
        get() = retrieveDataTableInfo()

    // по параметрам DatabaseParameterInfo подготовить описание таблицы
    fun refreshDataTableInfo(parameters: List<DatabaseParameterInfo>) {
        val dataTableInfo = DataTableInfo(mutableMapOf())

        // вот здесь происходит так называемое "лямбда-замыкание" за счет цикла forEach можно из л-в обновлять внешнюю переменную
        parameters.forEach {
            dataTableInfo.info.put(it.parametersListId, DataTableColumnInfo(
                it.paramName + " (" + it.mnemoCode + ")", it.paramPositions, it.paramPrecision))
        }

        // сохранить в шаред преференсез
        serializeDataTableInfo(dataTableInfo)
    }

    private fun serializeDataTableInfo(dataTableInfo: DataTableInfo) {
        val preferences = context.getSharedPreferences(FILE_NAME, MODE_PRIVATE)
        val editor = preferences.edit()

        // записать количество параметров для отображения
        editor.putInt(KEY_PARAMS_COUNT, dataTableInfo.info.size)

        // записать ключ-значение
        var index = 0
        dataTableInfo.info.forEach {
            editor.putInt("ID_" + index.toString(), it.key)
            editor.putString("NM_" + index.toString(), it.value.visibleColumnName)
            editor.putInt("PP_" + index.toString(), it.value.paramPositions)
            editor.putInt("PC_" + index.toString(), it.value.paramPrecision)
            index++
        }

        editor.apply()
    }

    private fun retrieveDataTableInfo(): DataTableInfo? {
        var result: DataTableInfo? = null // если описания таблицы нет в SharedPreferences, то возвращается null
        val preferences = context.getSharedPreferences(FILE_NAME, MODE_PRIVATE)

        // прочитать сколько параметров отображает таблица
        val paramsCount = preferences.getInt(KEY_PARAMS_COUNT, -1)

        if (paramsCount > 0) {
            result = DataTableInfo(mutableMapOf())

            for (ii in 0 until paramsCount) {
                val id = preferences.getInt("ID_" + ii.toString(), -1)
                if (id != -1) {
                    val nm = preferences.getString("NM_" + ii.toString(), "unknown")
                    val pp = preferences.getInt("PP_" + ii.toString(), 5)
                    val ps = preferences.getInt("PC_" + ii.toString(), 3)
                    result?.info?.put(id, DataTableColumnInfo(nm!!, pp, ps))
                } else {
                    result = null
                    break;
                }
            }
        }

        return result
    }

    //---------------------------------------------------------------------------------------------
    // сохранение - восстановление диапазонов дат-времен для загрузки данных и журнала устройств
    //---------------------------------------------------------------------------------------------
    private val DATA_SEL_DATETIME_BEGIN = 0
    private val DATA_SEL_DATETIME_END = 1
    private val LOG_SEL_DATETIME_BEGIN = 2
    private val LOG_SEL_DATETIME_END = 3

    // при смене активного объекта (с которым идет работа) свойству нужно присвоить null
    // в этом случае до записи корректных значений будет читаться также null и текстоыве поля заполняться не будут
    // вместо этого будет показываться hint а при попытке загрузить данные будет выдаваться соответствующее сообщение
    var deviceDataBeginDate: SelDate? // null может быть в случае если ранее ничего не было сохранено
        get() = getSharedSelDate(DATA_SEL_DATETIME_BEGIN)
        set(value) = setSharedSelDate(DATA_SEL_DATETIME_BEGIN, value)

    var deviceDataBeginTime: SelTime? // null может быть в случае если ранее ничего не было сохранено
        get() = getSharedSelTime(DATA_SEL_DATETIME_BEGIN)
        set(value) = setSharedSelTime(DATA_SEL_DATETIME_BEGIN, value)

    var deviceDataEndDate: SelDate?
        get() = getSharedSelDate(DATA_SEL_DATETIME_END)
        set(value) = setSharedSelDate(DATA_SEL_DATETIME_END, value)

    var deviceDataEndTime: SelTime?
        get() = getSharedSelTime(DATA_SEL_DATETIME_END)
        set(value) = setSharedSelTime(DATA_SEL_DATETIME_END, value)

    var deviceLogBeginDate: SelDate?
        get() = getSharedSelDate(LOG_SEL_DATETIME_BEGIN)
        set(value) = setSharedSelDate(LOG_SEL_DATETIME_BEGIN, value)

    var deviceLogBeginTime: SelTime?
        get() = getSharedSelTime(LOG_SEL_DATETIME_BEGIN)
        set(value) = setSharedSelTime(LOG_SEL_DATETIME_BEGIN, value)

    var deviceLogEndDate: SelDate?
        get() = getSharedSelDate(LOG_SEL_DATETIME_END)
        set(value) = setSharedSelDate(LOG_SEL_DATETIME_END, value)

    var deviceLogEndTime: SelTime?
        get() = getSharedSelTime(LOG_SEL_DATETIME_END)
        set(value) = setSharedSelTime(LOG_SEL_DATETIME_END, value)

    private fun getSharedSelDate(key: Int): SelDate? {
        var result: SelDate? = null
        val preferences = context.getSharedPreferences(FILE_NAME, MODE_PRIVATE)
        var year = 0
        var month = 0
        var day = 0
        // все поля должны быть заполнены
        year = preferences.getInt("DT_FIELD_${key}_YEAR", -1)
        if (year != -1) {
            month = preferences.getInt("DT_FIELD_${key}_MONTH", -1)
            if (month != -1) {
                day = preferences.getInt("DT_FIELD_${key}_DAY", -1)
                if (day != -1) {
                    result = SelDate(year, month, day)
                }
            }
        }
        return result
    }

    private fun getSharedSelTime(key: Int): SelTime? {
        var result: SelTime? = null
        val preferences = context.getSharedPreferences(FILE_NAME, MODE_PRIVATE)
        var hour = 0
        var minute = 0
        hour = preferences.getInt("DT_FIELD_${key}_HOUR", -1)
        if (hour != -1) {
            minute = preferences.getInt("DT_FIELD_${key}_MINUTE", -1)
            if (minute != -1) {
                result = SelTime(hour, minute)
            }
        }
        return result
    }

    private fun setSharedSelDate(key: Int, selDate: SelDate?) {
        val preferences = context.getSharedPreferences(FILE_NAME, MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putInt("DT_FIELD_${key}_YEAR", if (selDate != null) {selDate.year} else -1)
        editor.putInt("DT_FIELD_${key}_MONTH", if (selDate != null) {selDate.month} else -1)
        editor.putInt("DT_FIELD_${key}_DAY", if (selDate != null) {selDate.day} else -1)
        editor.apply()
    }

    private fun setSharedSelTime(key: Int, selTime: SelTime?) {
        val preferences = context.getSharedPreferences(FILE_NAME, MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putInt("DT_FIELD_${key}_HOUR", if (selTime != null) {selTime.hour} else -1)
        editor.putInt("DT_FIELD_${key}_MINUTE", if (selTime != null) {selTime.minute} else -1)
        editor.apply()
    }
}

private lateinit var INSTANCE: SharedPreferencesStorage

fun getSharedPreferencesStorage(context: Context): SharedPreferencesStorage {
    synchronized(SharedPreferencesStorage::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = SharedPreferencesStorage(context)
        }
    }
    return INSTANCE
}
