package com.example.android.mobileclient.utils

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.android.mobileclient.database.DeviceLogObject

const val CHANNEL_CODE_GSM = 2
const val CHANNEL_CODE_USB = 3
const val CHANNEL_CODE_SAT = 4

// показать текстовую расшифровку типа канала связи
@BindingAdapter("showChannel")
fun TextView.showChannel(data: DeviceLogObject) {
    text = when {
        data.channel == CHANNEL_CODE_GSM -> "GSM"
        data.channel == CHANNEL_CODE_USB -> "USB"
        data.channel == CHANNEL_CODE_SAT -> "Спутник"
        else -> "Unknown channel"
    }
}

// показать текстовый эквивалент уровня сигнала
@BindingAdapter("showSignalLevel")
fun TextView.showSignalLevel(data: DeviceLogObject) {
    text = when {
        data.signalLevel < 10 -> "Низкий"
        (data.signalLevel >= 10) && (data.signalLevel < 20) -> "Средний"
        data.signalLevel >= 20 -> "Высокий"
        else -> "Unknown level"
    }
}

// длительность сеанса связи из Int в String
@BindingAdapter("showSessionDuration")
fun TextView.showSessionDuration(data: DeviceLogObject) {
    text = data.duration.toString()
}
