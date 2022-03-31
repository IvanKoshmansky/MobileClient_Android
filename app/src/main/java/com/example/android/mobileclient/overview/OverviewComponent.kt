package com.example.android.mobileclient.overview

import com.example.android.mobileclient.di.ActivityScope
import com.example.android.mobileclient.main.MainActivity
import com.example.android.mobileclient.overview.detail.DetailFragment
import dagger.Subcomponent

@ActivityScope
@Subcomponent
interface OverviewComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): OverviewComponent
    }

    fun inject(fragment: OverviewFragment)
    fun inject(fragment: DetailFragment)
}
