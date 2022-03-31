package com.example.android.mobileclient.data.devicelog

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.mobileclient.R
import com.example.android.mobileclient.database.DeviceLogObject
import com.example.android.mobileclient.databinding.FragmentDeviceLogBinding
import com.example.android.mobileclient.main.MainActivity
import com.example.android.mobileclient.utils.LOADING_DIALOG_HIDE
import com.example.android.mobileclient.utils.LOADING_DIALOG_IDLE
import com.example.android.mobileclient.utils.LOADING_DIALOG_SHOW
import com.example.android.mobileclient.utils.LoadingDialogFragment
import javax.inject.Inject

class DeviceLogFragment : Fragment(), LoadingDialogFragment.LoadingDialogListener {

    //---------------------------------------------------------------------------------------------
    // ViewModel
    //---------------------------------------------------------------------------------------------

    @Inject
    lateinit var viewModel: DeviceLogViewModel

    private var viewModelAdapter: DeviceLogPagedListAdapter? = null

    //---------------------------------------------------------------------------------------------
    // onAttach() onAcivityCreated() onCreateView()
    //---------------------------------------------------------------------------------------------

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        (activity as MainActivity).deviceComponent.inject(this)
        (activity as MainActivity).dataComponent.inject(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.deviceLogList.observe(viewLifecycleOwner, Observer<PagedList<DeviceLogObject>> {
            it.apply {
                viewModelAdapter?.submitList(it)
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        //-----------------------------------------------------------------------------------------
        // инициализация, биндинг
        //-----------------------------------------------------------------------------------------
        val binding: FragmentDeviceLogBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_device_log, container, false)
        //для работы биндинга в xml прописать:
        //tools:context="com.example.android.mobileclient.data.devicelog.DeviceLogFragment"
        //android:id="@+id/fragment_device_log"
        binding.setLifecycleOwner(viewLifecycleOwner)
        binding.viewModel = viewModel

        //-----------------------------------------------------------------------------------------
        // RecyclerView & Adapter
        //-----------------------------------------------------------------------------------------
        viewModelAdapter = DeviceLogPagedListAdapter(viewModel)
        val recyclerView = binding.rwLogTable
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = viewModelAdapter
        }

        //-----------------------------------------------------------------------------------------
        // программная логика данного экрана: стыковка с интерфейсом
        //-----------------------------------------------------------------------------------------
        // вызов диалога задания начальной даты
        viewModel.triggerBeginDateDialog.observe(this, Observer {
            if (it == true) {
                viewModel.beginDate?.let {
                    val datePickerDialog = DatePickerDialog(context, DatePickerDialog.OnDateSetListener {
                        // в конструкторе DatePickerDialog.OnDateSetListener один параметр - лямбда выражение, круглые скобки не нужны
                        view: DatePicker?, year: Int, month: Int, dayOfMonth: Int -> viewModel.setBeginDateFromDialog(year, month, dayOfMonth)
                    }, it.year, it.month, it.day) // остальные параметры конструктора DatePickerDialog()
                    datePickerDialog.show()
                }
                viewModel.triggerBeginDateDialog.value = false
            }
        })

        // вызов диалога задания начального времени
        viewModel.triggerBeginTimeDialog.observe(this, Observer {
            if (it == true) {
                viewModel.beginTime?.let {
                    val timePickerDialog = TimePickerDialog(context, TimePickerDialog.OnTimeSetListener {
                        view, hourOfDay, minute -> viewModel.setBeginTimeFromDialog(hourOfDay, minute)
                    }, it.hour, it.minute, false)
                    timePickerDialog.show()
                }
                viewModel.triggerBeginTimeDialog.value = false
            }
        })

        // вызов диалога задания конечной даты
        viewModel.triggerEndDateDialog.observe(this, Observer {
            if (it == true) {
                viewModel.endDate?.let {
                    val datePickerDialog = DatePickerDialog(context, DatePickerDialog.OnDateSetListener {
                        view, year, month, dayOfMonth -> viewModel.setEndDateFromDialog(year, month, dayOfMonth)
                    }, it.year, it.month, it.day)
                    datePickerDialog.show()
                }
                viewModel.triggerEndDateDialog.value = false
            }
        })

        // вызов диалога задания конечного времени
        viewModel.triggerEndTimeDialog.observe(this, Observer {
            if (it == true) {
                viewModel.endTime?.let {
                    val timePickerDialog = TimePickerDialog(context, TimePickerDialog.OnTimeSetListener {
                        view, hourOfDay, minute -> viewModel.setEndTimeFromDialog(hourOfDay, minute)
                    }, it.hour, it.minute, false)
                    timePickerDialog.show()
                }
            }
        })

        // показ диолога о том, что нужно выбрать корректный интервал дат-времен
        viewModel.triggerInvalidDateTimeDialog.observe(this, Observer {
            if (it == true) {
                Toast.makeText(context, R.string.invalid_date_time_dialog, Toast.LENGTH_SHORT).show()
                viewModel.triggerInvalidDateTimeDialog.value = false
            }
        })

        viewModel.triggerConnectionErrorMessage.observe(this, Observer {
            if (it == true) {
                viewModel.triggerLoadingDialog.value = LOADING_DIALOG_HIDE
                Toast.makeText(context, R.string.connection_erroe_message, Toast.LENGTH_SHORT).show()
                viewModel.triggerConnectionErrorMessage.value = false
            }
        })

        var loadingDialogFragment: LoadingDialogFragment? = null

        // показ диалога при обновлении данных
        viewModel.triggerLoadingDialog.observe(this, Observer {
            if (it == LOADING_DIALOG_SHOW) {
                loadingDialogFragment = LoadingDialogFragment()
                loadingDialogFragment?.listener = this // присваиваем ссылку на класс, который реализовал интерфейс
                loadingDialogFragment?.show(getFragmentManager(), LoadingDialogFragment.TAG)
                viewModel.triggerLoadingDialog.value = LOADING_DIALOG_IDLE
            }
            if (it == LOADING_DIALOG_HIDE) {
                loadingDialogFragment?.dismiss()
                viewModel.triggerLoadingDialog.value = LOADING_DIALOG_IDLE
            }
        })

        //-----------------------------------------------------------------------------------------
        // возврат binding.root
        //-----------------------------------------------------------------------------------------
        return binding.root
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        viewModel.cancelLoading()
    }
}
