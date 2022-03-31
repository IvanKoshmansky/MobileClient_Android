package com.example.android.mobileclient.di

import android.app.Application
import com.example.android.mobileclient.data.DataComponent
import com.example.android.mobileclient.device.DeviceManager
import com.example.android.mobileclient.directory.DirectoryManager
import com.example.android.mobileclient.main.MainComponent
import com.example.android.mobileclient.overview.OverviewComponent
import com.example.android.mobileclient.paramdetail.ParamDetailComponent
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

//
// памятка
//

// Use @BindsInstance for objects that are constructed outside of the graph (e.g. instances of Context).
// TODO: еще раз уточнить отличие field injection от constructor injection

@Singleton
@Component(modules = [StorageModule::class, AppSubcomponents::class])
interface AppComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance application: Application): AppComponent
    }

    fun mainComponent(): MainComponent.Factory
    fun overviewComponent(): OverviewComponent.Factory
    fun paramDetailComponent(): ParamDetailComponent.Factory
    fun dataComponent(): DataComponent.Factory

    fun deviceManager(): DeviceManager
    fun directoryManager(): DirectoryManager
}
