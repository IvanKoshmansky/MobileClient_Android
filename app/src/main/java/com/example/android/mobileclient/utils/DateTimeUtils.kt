package com.example.android.mobileclient.utils

import java.sql.Timestamp
import java.util.*

data class TimestampRange (
    val begin: Timestamp,
    val end: Timestamp
)

data class SelDate (
    var year: Int,
    var month: Int,
    var day: Int
)

data class SelTime (
    var hour: Int,
    var minute: Int
)

// объект - синглтон и его статические методы (для удобства)
object DateTimeUtils {
    fun dateTimeRangeToTimestampRange(beginYear: Int, beginMonth: Int, beginDay: Int, beginHour: Int, beginMinute: Int,
                                      endYear: Int, endMonth: Int, endDay: Int, endHour: Int, endMinute: Int): TimestampRange {
        val calendarBegin = GregorianCalendar()
        calendarBegin.set(beginYear, beginMonth, beginDay, beginHour, beginMinute)
        val calendarEnd = GregorianCalendar()
        calendarEnd.set(endYear, endMonth, endDay, endHour, endMinute)
        val beginStamp = Timestamp(calendarBegin.getTime().getTime())
        val endStamp = Timestamp(calendarEnd.getTime().getTime())
        return TimestampRange(beginStamp, endStamp)
    }

    fun dateTimeToMsSqlServerFormat(timestamp: Timestamp): String {
        val calendar = GregorianCalendar()
        calendar.setTimeInMillis(timestamp.getTime());
        val year = calendar.get(Calendar.YEAR);
        val month = calendar.get(Calendar.MONTH);
        val day = calendar.get(Calendar.DAY_OF_MONTH);
        val hour = calendar.get(Calendar.HOUR_OF_DAY);
        val minute = calendar.get(Calendar.MINUTE);
        return String.format(Locale.ENGLISH, "'%04d%02d%02d %02d:%02d:00'", year, month + 1, day, hour, minute)
    }
}
