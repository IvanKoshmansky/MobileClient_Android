package com.example.android.mobileclient.paramdetail

import com.example.android.mobileclient.di.ActivityScope
import com.example.android.mobileclient.main.MainActivity
import dagger.Subcomponent

@ActivityScope
@Subcomponent
interface ParamDetailComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): ParamDetailComponent
    }

    fun inject(fragment: ParamDetailFragment)
}
