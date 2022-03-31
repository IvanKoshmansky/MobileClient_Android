package com.example.android.mobileclient.overview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.mobileclient.databinding.OverviewItemBinding
import com.example.android.mobileclient.device.DomainDeviceInfo

class OverviewListAdapter (val clickListener: OverviewClickListener) : ListAdapter<DomainDeviceInfo, OverviewListAdapter.ViewHolder> (
    DeviceInfoDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, clickListener)
    }

    class ViewHolder private constructor(val binding: OverviewItemBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(item: DomainDeviceInfo, clickListener: OverviewClickListener) {
            binding.deviceInfo = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = OverviewItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    class DeviceInfoDiffCallback: DiffUtil.ItemCallback<DomainDeviceInfo>() {

        override fun areItemsTheSame(oldItem: DomainDeviceInfo, newItem: DomainDeviceInfo): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: DomainDeviceInfo, newItem: DomainDeviceInfo): Boolean {
            return oldItem == newItem
        }
    }
}

class OverviewClickListener(val clickListener: (objectId: Int) -> Unit) {
    fun onClick(deviceInfo: DomainDeviceInfo) = clickListener(deviceInfo.objectId)
}
