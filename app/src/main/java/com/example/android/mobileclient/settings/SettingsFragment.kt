package com.example.android.mobileclient.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.example.android.mobileclient.R
import com.example.android.mobileclient.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private val viewModel: SettingsViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProviders.of(this, SettingsViewModel.Factory(activity.application))
            .get(SettingsViewModel::class.java)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//        Timber.i("SettingsFragment_Logging")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding: FragmentSettingsBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_settings, container, false)

        binding.setLifecycleOwner(viewLifecycleOwner) // стандартный код

        binding.viewModel = viewModel

        return binding.root
    }
}
