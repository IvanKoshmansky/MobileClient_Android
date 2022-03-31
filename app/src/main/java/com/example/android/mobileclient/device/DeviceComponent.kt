package com.example.android.mobileclient.device

import com.example.android.mobileclient.data.DataFragment
import com.example.android.mobileclient.data.devicelog.DeviceLogFragment
import com.example.android.mobileclient.overview.OverviewFragment
import com.example.android.mobileclient.overview.detail.DetailFragment
import dagger.Subcomponent

@DeviceScope
@Subcomponent
interface DeviceComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): DeviceComponent
    }

    fun inject(overviewFragment: OverviewFragment)
    fun inject(detailFragment: DetailFragment)
    fun inject(dataFragment: DataFragment)
    fun inject(deviceLogFragment: DeviceLogFragment)
}
