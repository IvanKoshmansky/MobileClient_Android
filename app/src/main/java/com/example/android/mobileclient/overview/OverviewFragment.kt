package com.example.android.mobileclient.overview

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.mobileclient.R
import com.example.android.mobileclient.databinding.FragmentOverviewBinding
import com.example.android.mobileclient.device.DomainDeviceInfo
import com.example.android.mobileclient.main.MainActivity
import javax.inject.Inject

class OverviewFragment : Fragment() {

    @Inject
    lateinit var overviewViewModel: OverviewViewModel

    private var viewModelAdapter: OverviewListAdapter? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // после onAttach
        (activity as MainActivity).deviceComponent.inject(this)
        (activity as MainActivity).overviewComponent.inject(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        overviewViewModel.setupView()
        overviewViewModel.deviceInfos.observe(viewLifecycleOwner, Observer<List<DomainDeviceInfo>> {
            deviceInfos -> deviceInfos.apply {
                viewModelAdapter?.submitList(deviceInfos)
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding: FragmentOverviewBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_overview,
            container, false)

        binding.setLifecycleOwner(viewLifecycleOwner)

        binding.viewModel = overviewViewModel

        viewModelAdapter = OverviewListAdapter(OverviewClickListener {
            objectId -> overviewViewModel.onDeviceClicked(objectId) // обработчик нажатий на элементы списка
        })

        // навигация: adapter -> callback -> переменная-триггер -> обсервер -> навигация (с аргументом)
        overviewViewModel.navigateToDeviceDetail.observe(this, Observer {
            objectId -> objectId?.let {
                this.findNavController().navigate(OverviewFragmentDirections.actionOverviewFragmentToDetailFragment(objectId))
                overviewViewModel.onDeviceDetailNavigated() // objectId <- null
            }
        })

        binding.overviewList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = viewModelAdapter
        }

        // текствотчер для навигации по объектам в списке
        // механизм: текствотчер->измененный текст->переход во view model->запрос в room->
        // возврат инфо объекта->по инфо определить позицию в списке (который ранее передали через submit list)->
        // позиция найдена->триггер скроллинга->возврат во фрагмент по обсерверу->выполнить скроллинг->
        // отметить скроллинг выполненным
        binding.overviewNameEdit.addTextChangedListener(object : TextWatcher { // здесь создается анонимный класс
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // по тексту в EditText найти устройство с вхождением подстроки в имя устройства
                s?.let {
                    overviewViewModel.triggerScrollingToDeviceName(it.toString())
                }
            }
        })

        // TODO: разобраться как работает .scrollToPosition()
        overviewViewModel.scrollToDevicePos.observe(this, Observer { position ->
            position?.let {
                if (position >= 0) {
                    binding.overviewList.scrollToPosition(position)
                    overviewViewModel.setScrollingDone()
                }
            }
        })

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.overview_overflow_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return NavigationUI.onNavDestinationSelected(item, view!!.findNavController())
            || super.onOptionsItemSelected(item)
    }
}
