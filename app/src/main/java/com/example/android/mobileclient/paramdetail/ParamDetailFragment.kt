package com.example.android.mobileclient.paramdetail

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.mobileclient.R
import com.example.android.mobileclient.databinding.FragmentParamDetailBinding
import com.example.android.mobileclient.directory.DirectoryManager
import com.example.android.mobileclient.directory.DomainParameterInfo
import com.example.android.mobileclient.main.MainActivity
import javax.inject.Inject

class ParamDetailFragment : Fragment() {

    @Inject
    lateinit var paramDetailViewModel: ParamDetailViewModel

    private var viewModelAdapter: ParamDetailListAdapter? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // после onAttach
        (activity as MainActivity).paramDetailComponent.inject(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        paramDetailViewModel.parameterInfos.observe(viewLifecycleOwner, Observer<List<DomainParameterInfo>> {
            parameterInfos -> parameterInfos.apply {
                viewModelAdapter?.submitList(parameterInfos)
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding: FragmentParamDetailBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_param_detail,
            container, false)

        binding.setLifecycleOwner(viewLifecycleOwner)

        binding.viewModel = paramDetailViewModel

        viewModelAdapter = ParamDetailListAdapter(paramDetailViewModel)

        binding.root.findViewById<RecyclerView>(R.id.param_detail_list).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = viewModelAdapter
        }

        return binding.root
    }
}
