package com.example.android.mobileclient.other

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.android.mobileclient.R
import com.example.android.mobileclient.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding: FragmentAboutBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_about, container, false)

        return binding.root
    }
}
