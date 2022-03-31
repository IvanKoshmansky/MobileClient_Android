package com.example.android.mobileclient.database

import android.content.ContentValues
import android.database.sqlite.SQLiteException
import androidx.room.OnConflictStrategy
import com.example.android.mobileclient.data.DataPagingObject
import com.example.android.mobileclient.network.NetworkDataResponce
import com.example.android.mobileclient.storage.SharedPreferencesStorage
import java.lang.NumberFormatException
import java.util.*
import javax.inject.Inject

//
// функции для работы с БД по-старому (через API SQLite)
//

//
// таблица с данными (формируется динамически) будет иметь столбцы:
// id (с нуля и по возрастанию, нужно для адаптера) ; признак тревожной строки ; дата-время ; столбец с ParametersListId = M ;
// столбец с ParametersListId = N ; столбец с ParametersListId = K ...
// соответствие между ParametersListId и заголовком столбца для отображения будет устаналиваться
// отдельным запросом к Storage
//

data class DataTableCursorObject (
    val isAlarm: Boolean,                 // признак тревожности (красная строка, когда от сервера приходит reason == valueTxt)
    val id: Int,                          // id строки начиная с нуля
    val dateTime: String,                 // дата-время строки в строковом формате
    val columns: MutableMap<Int, String>  // значение каждого столбца в виде "id параметра" - "стороковое значение"
)

fun List<DataTableCursorObject>.asDomainModel(): List<DataPagingObject> {
    return map {
        DataPagingObject(it.id, it.dateTime, it.isAlarm, it.columns)
    }
}

class SQLiteDatabase @Inject constructor (val database: LocalDatabase, val sharedPreferencesStorage: SharedPreferencesStorage) {

    val supportSQLiteDatabase = database.getOpenHelper().getWritableDatabase()

    // фиксированный набор столбцов
    private val DB_TABLE_DATA = "data_table"            // название таблицы
    private val DB_TABLE_DATA_ID = "_id"                // id с нуля для адаптера
    private val DB_TABLE_DATA_ALARM = "alarm"           // признак тревожной строки
    private val DB_TABLE_DATA_DATETIME = "date_time"    // дата-время
    // далее столбцы идут с названиями, равными ParametersListId параметра для отображения + префикс
    private val DB_TABLE_DATA_COLUMN_PREFIX = "col_"

    private val DB_CREATE_DATA_TEMPLATE = "create table " + DB_TABLE_DATA + "(" +
            DB_TABLE_DATA_ID + " integer primary key autoincrement, " +
            DB_TABLE_DATA_ALARM + " integer, " +
            DB_TABLE_DATA_DATETIME + " text, "

    private var _id = 0

    // функция создания таблицы с нужными параметрами для отображения
    fun prepareDataTable() {
        var sqlCreateTable = DB_CREATE_DATA_TEMPLATE;
        val dataTableInfo = sharedPreferencesStorage.dataTableInfo

        dataTableInfo?.let {
            _id = 0

            val keys= it.info.keys.toTypedArray()

            for (ii in 0 until keys.size) {
                sqlCreateTable += if (ii < keys.size - 1) { DB_TABLE_DATA_COLUMN_PREFIX + keys[ii].toString() + " text, " } else { DB_TABLE_DATA_COLUMN_PREFIX + keys[ii].toString() + " text)" }
            }

            supportSQLiteDatabase.beginTransaction()

            try {
                supportSQLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_DATA)
                supportSQLiteDatabase.execSQL(sqlCreateTable)
                supportSQLiteDatabase.setTransactionSuccessful()
            } finally {
                supportSQLiteDatabase.endTransaction()
            }
        }
    }

    // добавить данные в конец таблицы
    fun addDataToTable(networkDataResponce: MutableList<NetworkDataResponce>) {
        val dataTableInfo = sharedPreferencesStorage.dataTableInfo
        dataTableInfo?.let {

            var parametersListId = 0
            var columnName = "" // определяется по parametersListId + префикс
            var dateTime = ""
            var lastDateTime = ""
            var valueTxt = ""
            var alarmState = false
            var lastAlarmState = false
            var paramPositions = 0
            var paramPrecision = 0
            var fmt = ""
            var fvalue = 0.0f
            val contentValues = arrayOf(ContentValues(), ContentValues()) // два элемента
            var contentIndex = 0

            try {
                supportSQLiteDatabase.beginTransaction()

                for (item in networkDataResponce) {
                    parametersListId = item.parametersListId
                    if (it.info.containsKey(parametersListId)) {
                        // параметр с данным ID присутствует в колонках таблицы
                        val columnInfo = it.info.get(parametersListId)

                        columnName = DB_TABLE_DATA_COLUMN_PREFIX + parametersListId.toString()
                        dateTime = item.valueDateTime.substring(0, 16)
                        valueTxt = item.paramValueTXT
                        alarmState = item.reason.equals(valueTxt)
                        paramPositions = columnInfo!!.paramPositions
                        paramPrecision = columnInfo!!.paramPrecision
                        fmt = "%${paramPositions + paramPrecision}.${paramPrecision}f"

                        if (paramPrecision != 0) {
                            try {
                                fvalue = valueTxt.toFloat()
                                valueTxt = String.format(Locale.ENGLISH, fmt, fvalue)
                            } catch (e: NumberFormatException) {
                                e.printStackTrace()
                            }
                        }

                        if (!dateTime.equals((lastDateTime))) {
                            if (contentValues[0].size() != 0) {
                                supportSQLiteDatabase.insert(DB_TABLE_DATA, OnConflictStrategy.REPLACE, contentValues[0])
                            }
                            if (contentValues[1].size() != 0) {
                                supportSQLiteDatabase.insert(DB_TABLE_DATA, OnConflictStrategy.REPLACE, contentValues[1])
                            }
                            lastDateTime = dateTime
                            lastAlarmState = alarmState
                            contentIndex = 0
                            contentValues[0].clear()
                            contentValues[1].clear()
                            contentValues[0].put(DB_TABLE_DATA_ID, _id)
                            contentValues[0].put(DB_TABLE_DATA_DATETIME, dateTime)
                            contentValues[0].put(DB_TABLE_DATA_ALARM, if (alarmState) 1 else 0)
                            _id++
                        } else if (alarmState != lastAlarmState) {
                            contentIndex = if (contentIndex == 0) 1 else 0
                            if (contentValues[contentIndex].size() == 0) {
                                contentValues[contentIndex].put(DB_TABLE_DATA_ID, _id)
                                contentValues[contentIndex].put(DB_TABLE_DATA_DATETIME, dateTime)
                                contentValues[contentIndex].put(DB_TABLE_DATA_ALARM, if (alarmState) 1 else 0)
                                _id++
                            }
                        }

                        contentValues[contentIndex].put(columnName, valueTxt)
                        lastDateTime = dateTime
                        lastAlarmState = alarmState
                    }
                }

                if (contentValues[0].size() != 0) {
                    supportSQLiteDatabase.insert(DB_TABLE_DATA, OnConflictStrategy.REPLACE, contentValues[0])
                }
                if (contentValues[1].size() != 0) {
                    supportSQLiteDatabase.insert(DB_TABLE_DATA, OnConflictStrategy.REPLACE, contentValues[1])
                }

                // транзакция выполнена успешно
                supportSQLiteDatabase.setTransactionSuccessful()

            } catch (e: SQLiteException) {
                e.printStackTrace()
            } finally {
                supportSQLiteDatabase.endTransaction()
            }
        }
    }

    // узнать сколько строк в таблице
    fun getDataTableRowsCount(): Int {
        var result = 0
        try {
            val cursor = supportSQLiteDatabase.query("select count(*) as count from $DB_TABLE_DATA")
            cursor.moveToFirst()
            result = cursor.getInt(cursor.getColumnIndex("count"))
            cursor.close()
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }
        return result
    }

    // удалить все данные из таблицы data_table
    fun deleteAllDeviceData() {
        supportSQLiteDatabase.beginTransaction()
        try {
            supportSQLiteDatabase.execSQL("delete from $DB_TABLE_DATA")
            supportSQLiteDatabase.setTransactionSuccessful()
        } catch (e: SQLiteException) {
            e.printStackTrace()
        } finally {
            supportSQLiteDatabase.endTransaction()
        }
    }

    // получить данные для адапптера в формате DataTableCursorObject (DataTableCursorObject<-Cursor)
    // startPosition с нуля, для адаптера
    // loadSize количество строк для подгрузки
    fun getDataTableRows(startPosition: Int, loadSize: Int): List<DataTableCursorObject> {
        val outList = mutableListOf<DataTableCursorObject>()
        try {
            val dataTableInfo = sharedPreferencesStorage.dataTableInfo
            val cursor = supportSQLiteDatabase.query("select * from $DB_TABLE_DATA where " +
                "($DB_TABLE_DATA_ID >= $startPosition) and ($DB_TABLE_DATA_ID < ${startPosition + loadSize})")
            cursor.moveToFirst()
            for (ii in 0 until cursor.count) {
                val _id = cursor.getInt(cursor.getColumnIndex(DB_TABLE_DATA_ID))
                val alarm = cursor.getInt(cursor.getColumnIndex(DB_TABLE_DATA_ALARM)) == 1
                val datetime = cursor.getString(cursor.getColumnIndex(DB_TABLE_DATA_DATETIME))
                val dataTableCursorObject = DataTableCursorObject(alarm, _id, datetime, mutableMapOf())
                dataTableInfo?.info?.map {
                    // значение параметра <- имя колонки: префикс + id параметра (it.key)
                    val key = it.key
                    // ситуация когда настроено отображение параметра, данных по которому у данного устройства нет
                    // в этом случае возвращается null, поэтому подставляем пустую строку
                    val textValue = cursor.getString(cursor.getColumnIndex(DB_TABLE_DATA_COLUMN_PREFIX + it.key)) ?: ""
                    dataTableCursorObject.columns.put(key, textValue)
                }
                outList.add(dataTableCursorObject)
                cursor.moveToNext()
            }
            cursor.close()
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }
        return outList
    }
}
