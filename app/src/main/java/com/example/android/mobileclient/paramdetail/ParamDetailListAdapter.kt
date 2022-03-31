package com.example.android.mobileclient.paramdetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.mobileclient.databinding.ParamDetailItemBinding
import com.example.android.mobileclient.directory.DomainParameterInfo
import kotlinx.coroutines.launch

class ParamDetailListAdapter (val paramDetailViewModel: ParamDetailViewModel) :
    ListAdapter<DomainParameterInfo, ParamDetailListAdapter.ViewHolder>(
        DeviceInfoDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewHolder = ViewHolder.from(parent)

        // TODO: проконсультироваться, возможно ли прописать этот обрбаботчик напрямую в xml по аналогии с OnClick?
        viewHolder.binding.paramDetailShowOnTableChb.setOnCheckedChangeListener { _, b ->
            paramDetailViewModel.uiScope.launch {
                paramDetailViewModel.directoryManager.setShowOnTable(viewHolder.binding.parameterInfo!!, b)
            }
        }
        viewHolder.binding.paramDetailShowOnChartChb.setOnCheckedChangeListener { _, b ->
            paramDetailViewModel.uiScope.launch {
                paramDetailViewModel.directoryManager.setShowOnChart(viewHolder.binding.parameterInfo!!, b)
            }
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class ViewHolder private constructor (val binding: ParamDetailItemBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(item: DomainParameterInfo) {
            binding.parameterInfo = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ParamDetailItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    // TODO: уточнить правила сравнения, возможно все-таки наоборот
    class DeviceInfoDiffCallback: DiffUtil.ItemCallback<DomainParameterInfo>() {

        override fun areItemsTheSame(oldItem: DomainParameterInfo, newItem: DomainParameterInfo): Boolean {
            return oldItem.paramUniqueId == newItem.paramUniqueId
        }

        override fun areContentsTheSame(oldItem: DomainParameterInfo, newItem: DomainParameterInfo): Boolean {
            return oldItem == newItem
        }
    }
}
