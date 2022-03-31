package com.example.android.mobileclient.data.devicelog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.android.mobileclient.database.DeviceLogObject
import com.example.android.mobileclient.databinding.DeviceLogItemBinding

class DeviceLogPagedListAdapter (val deviceLogViewModel: DeviceLogViewModel) :
    PagedListAdapter<DeviceLogObject, DeviceLogPagedListAdapter.ViewHolder>(DeviceLogDiffCallback()) {

    //---------------------------------------------------------------------------------------------
    // onCreateViewHolder() onBindViewHolder()
    //---------------------------------------------------------------------------------------------

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    //---------------------------------------------------------------------------------------------
    // ViewHolder
    //---------------------------------------------------------------------------------------------

    class ViewHolder private constructor (val binding: DeviceLogItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DeviceLogObject?) {
            binding.deviceLogItem = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                // типовой шаблон
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = DeviceLogItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

//-------------------------------------------------------------------------------------------------
// DiffCallback
//-------------------------------------------------------------------------------------------------

class DeviceLogDiffCallback : DiffUtil.ItemCallback<DeviceLogObject> () {
    override fun areItemsTheSame(oldItem: DeviceLogObject, newItem: DeviceLogObject): Boolean {
        // сравнение по id
        return oldItem._id == newItem._id
    }

    override fun areContentsTheSame(oldItem: DeviceLogObject, newItem: DeviceLogObject): Boolean {
        // сравнение по содержимому
        return oldItem == newItem
    }
}
