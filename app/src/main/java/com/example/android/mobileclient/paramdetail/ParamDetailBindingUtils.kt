package com.example.android.mobileclient.paramdetail

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.android.mobileclient.directory.DomainParameterInfo

//
// кастомные байндинг адаптеры: "объект для отображения" <- "объект из вью модел"
//

// тип устройства
@BindingAdapter("param_detail_devTypeIdString")
fun TextView.setDevTypeIdString(item: DomainParameterInfo?) {
    item?.let {
            text = when (item.devtypeId) {
                5 -> "БКПКМ"
                9 -> "ПЭКЗ"
                else -> "неизвестное устройство"
        }
    }
}
