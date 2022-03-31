package com.example.android.mobileclient.overview.detail

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.android.mobileclient.R
import com.example.android.mobileclient.databinding.FragmentDetailBinding
import com.example.android.mobileclient.main.MainActivity
import com.example.android.mobileclient.utils.LoadingDialogFragment
import javax.inject.Inject

//
// фрагмент со свойствами объекта (устройства): общая информация, версия прошивки, место установки и т.д.
//

class DetailFragment : Fragment() {

    @Inject
    lateinit var detailViewModel: DetailViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // после onAttach
        (activity as MainActivity).deviceComponent.inject(this)
        (activity as MainActivity).overviewComponent.inject(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // место для обсерверов списков (при необходимости)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding: FragmentDetailBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail,
            container, false)

        // извлечение параметров при переходе по навигации
        val arguments = DetailFragmentArgs.fromBundle(arguments!!)

        // установить ID текущего устройства для работы
        detailViewModel.setupDevice(arguments.objectId)

        binding.setLifecycleOwner(viewLifecycleOwner)

        binding.viewModel = detailViewModel

        detailViewModel.navigateToDeviceData.observe(this, Observer {
            if (it == true) {
                this.findNavController().navigate(DetailFragmentDirections.actionDetailFragmentToDataFragment())
                detailViewModel.onDeviceDataNavigated()
            }
        })

        detailViewModel.navigateToDeviceLog.observe(this, Observer {
            if (it == true) {
                this.findNavController().navigate(DetailFragmentDirections.actionDetailFragmentToDeviceLogFragment())
                detailViewModel.onDeviceLogNavigated()
            }
        })

        return binding.root
    }
}
