package com.example.android.mobileclient.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import com.example.android.mobileclient.directory.DomainParameterInfo

const val PARAMETER_TYPE_CLASS_ID_READABLE = 1
const val PARAMETER_TYPE_CLASS_ID_ARCHIVE = 4

// ВАЖНОЕ ЗАМЕЧАНИЕ: для работы с транзакциями Dao должен быть не интерфейс, а абстрактный класс

@Dao
abstract class DatabaseDao {

    @Query("select * from databasesettings")
    abstract fun getSettings(): LiveData<List<DatabaseSettings>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAllSettings(vararg settings: DatabaseSettings)

    @Query("select * from device_info_table")
    abstract fun getDeviceInfos(): LiveData<List<DatabaseDeviceInfo>>

    // не использовать для стыковки с активити и фрагментами
    @Query("select devtype_id from device_info_table")
    abstract fun getAllDevTypeIds(): List<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAllDeviceInfos(vararg deviceInfo: DatabaseDeviceInfo)

    // на сервере могут поменяться некоторые атрибуты параметра (например, количество знаков
    // после запятой в отображении), но при этом Id - поля останутся прежними
    // в этом случае обновляем информацию о данном параметре в room с заменой полей (.REPLACE),
    // но настройки отображения оставляем прежние (.IGNORE)
    // итоговый запрос к room делается к двум связанным таблицам через транзакцию
    // ВНИАМНИЕ: конфликт-стратегии применяются к первичным ключам при их совпадении
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAllParameterInfosToInfoTable(vararg databaseParameterInfo: DatabaseParameterInfo)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insertAllParameterInfosToShowTable(vararg databaseParameterInfoShow: DatabaseParameterInfoShow)

    // транзакция не является абстрактным методом
    // еще один нюанс: транзакция должна быть с модификатором "open" в том случае,
    // если ее метод не содержит реализацию в классе Dao
    @Transaction
    open fun insertAllParameterInfos(databaseParameterInfos: List<DatabaseParameterInfo>) {
        val databaseParameterInfosShow = databaseParameterInfos.map {
            DatabaseParameterInfoShow (
                _paramUniqueId = it.paramUniqueId
            )
        }
        insertAllParameterInfosToInfoTable(*databaseParameterInfos.toTypedArray())
        insertAllParameterInfosToShowTable(*databaseParameterInfosShow.toTypedArray())
    }

    // благодаря @Embedded и @Relation запрос к основной таблице "parameter_info_table" вытащит данные
    // из связанной дочерней таблицы "parameter_info_show_table"
    // запрос в итоге составной, поэтому транзакция необходима
    // ВНИМАНИЕ: для прямой стыковки с LiveData нужно было обновить версию рума и прописать
    // зависимость annotationProcessor "androidx.room:room-compiler:$version_room"
    // НА БУДУЩЕЕ: для запроса из связанных таблиц обязательно использовать @Relation,
    // длинные запросы писать руками нельзя - производительность при выводе на экран будет ниже плинтуса
    @Transaction
    @Query("select * from parameter_info_table where parameter_info_table.parameter_type_class_id = $PARAMETER_TYPE_CLASS_ID_ARCHIVE")
    abstract fun getArchiveParameterInfos(): LiveData<List<DatabaseParameterInfoAndShow>>

    @Update
    abstract fun updateParameterInfoShow(databaseParameterInfosShow: DatabaseParameterInfoShow)

    // медленный запрос (выполняется однократно при нажатии на кнопку) можно написать "руками" без @Relation
    @Query("select * from parameter_info_table, parameter_info_show_table where (parameter_info_table.parameter_type_class_id = $PARAMETER_TYPE_CLASS_ID_ARCHIVE) and (parameter_info_table.param_unique_id == parameter_info_show_table._param_unique_id) and (parameter_info_show_table.show_on_table = 1)")
    abstract fun getSelectedArchiveParameterInfosForData() : List<DatabaseParameterInfo>

    // получить описание Readble параметров синхронизированных с БД сервера
    @Query("select * from parameter_info_table where parameter_info_table.parameter_type_class_id = $PARAMETER_TYPE_CLASS_ID_READABLE and parameter_info_table.devtype_id = :devtypeId")
    abstract fun getReadableParams(devtypeId: Int): List<DatabaseParameterInfo>

    // переменные вводятся в запрос с помощью двоеточия
    @Query("select * from device_info_table where object_id = :objectId")
    abstract fun getDeviceInfoById(objectId: Int): DatabaseDeviceInfo

    // получить объект устройства с вхождением строки "s" в колонке name
    // здесь строка s должна быть подготовлена в формате '%то что надо найти%' чтобы корректно ввести переменную в запрос
    // кроме того, возвращаемый тип должен быть обязательно null - совместимым,
    // поскольку если ничего не найдено, то должна быть возможность вернуть null (иначе будет NullPointerException)
    @Query("select * from device_info_table where name like :s")
    abstract fun getDeviceBySubstringInName(s: String): DatabaseDeviceInfo?

    // вставка новых данных в таблицу device_log
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertDeviceLog(vararg deviceLog: DeviceLogObject)

    // извлечение данных из таблицы device_log
    // здесь room генерирует фабрику для использования во ViewModel
    @Query("select * from device_log_table")
    abstract fun deviceLogByIndex(): DataSource.Factory<Int, DeviceLogObject>

    // удалить все данные из таблицы device_log_table
    @Query("delete from device_log_table")
    abstract fun deleteDeviceLog()
}

// нюанс: возвращаемый тип DatabaseParameterInfoAndShow, который не хранится в отдельной таблице, в списке "entities" не указываем
@Database(entities = [DatabaseSettings::class,
                      DatabaseDeviceInfo::class,
                      DatabaseParameterInfo::class,
                      DatabaseParameterInfoShow::class,
                      DeviceLogObject::class], version = 1)
abstract class LocalDatabase: RoomDatabase() {
    abstract val databaseDao: DatabaseDao
}

private lateinit var INSTANCE: LocalDatabase

fun getDatabase(context: Context): LocalDatabase {
    synchronized(LocalDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext,
                LocalDatabase::class.java,
                "newdb31").build() // про миграцию есть отдельные статьи
        }
    }
    return INSTANCE
}

/**
 * возвращаемые типы для запроса из связанных таблиц (не являются Entity)
 */

// объект для возврата значений из связанных таблиц
data class DatabaseParameterInfoAndShow constructor (
    // Embedded entity для основого, родительского класса
    @Embedded val databaseParameterInfo: DatabaseParameterInfo,
    // Relaition для реализации связи "один-к-одному"
    @Relation (
        parentColumn = "param_unique_id",  // первичный ключ основной таблицы (parent)
        entityColumn = "_param_unique_id"  // поле дочерней таблицы (child) которое содержит первичный ключ основной таблицы
    )
    // дочерняя таблица
    val databaseParameterInfoShow: DatabaseParameterInfoShow
)

// преобразование для нужд LiveData
fun List<DatabaseParameterInfoAndShow>.asDomainParameterInfo(): List<DomainParameterInfo> {
    return map {
        DomainParameterInfo (
            paramUniqueId = it.databaseParameterInfo.paramUniqueId,
            devtypeId = it.databaseParameterInfo.devtypeId,
            parametersListId = it.databaseParameterInfo.parametersListId,
            parameterTypesId = it.databaseParameterInfo.parameterTypesId,
            parameterTypeClassId = it.databaseParameterInfo.parameterTypeClassId,
            paramName = it.databaseParameterInfo.paramName,
            mnemoCode = it.databaseParameterInfo.mnemoCode,
            measUnit = it.databaseParameterInfo.measUnit,
            paramPositions = it.databaseParameterInfo.paramPositions,
            paramPrecision = it.databaseParameterInfo.paramPrecision,
            showOnTable = it.databaseParameterInfoShow.showOnTable,
            showOnChart = it.databaseParameterInfoShow.showOnChart
        )
    }
}
