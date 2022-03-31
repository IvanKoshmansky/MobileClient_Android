package com.example.android.mobileclient.utils

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.android.mobileclient.R

const val LOADING_DIALOG_IDLE = 0
const val LOADING_DIALOG_SHOW = 1
const val LOADING_DIALOG_HIDE = 2

class LoadingDialogFragment : DialogFragment() {

    // интерфейс для реализации программной логики нажатий в вызывающем классе
    lateinit var listener: LoadingDialogListener

    interface LoadingDialogListener {
        fun onDialogNegativeClick(dialog: DialogFragment)
    }

    // реализация структуры диалога
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // inflater для кастомного содержимого средней части диалога
            val inflater = requireActivity().layoutInflater
            val customView = inflater.inflate(R.layout.dialog_fragment_loading, null)

            // сборка через билдер + кастомное содержимое посередине
            val builder = AlertDialog.Builder(it)

            builder.setView(customView)
                   .setMessage(R.string.loading_dialog_title)
                   .setNegativeButton(R.string.loading_dialog_cancel_btn,
                       DialogInterface.OnClickListener { dialog, id ->
                           listener.onDialogNegativeClick(this)
                       })

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)
        listener.onDialogNegativeClick(this)
    }

    companion object {
        const val TAG = "LoadingDialog"
    }
}
