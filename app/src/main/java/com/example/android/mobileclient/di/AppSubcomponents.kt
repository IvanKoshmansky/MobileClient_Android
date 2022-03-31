package com.example.android.mobileclient.di

import com.example.android.mobileclient.data.DataComponent
import com.example.android.mobileclient.device.DeviceComponent
import com.example.android.mobileclient.directory.DirectoryComponent
import com.example.android.mobileclient.main.MainComponent
import com.example.android.mobileclient.overview.OverviewComponent
import com.example.android.mobileclient.paramdetail.ParamDetailComponent
import dagger.Module

//
// TODO: добавить SettingsComponent
//

@Module(subcomponents = [MainComponent::class,
                         DeviceComponent::class,
                         DirectoryComponent::class,
                         OverviewComponent::class,
                         ParamDetailComponent::class,
                         DataComponent::class])
class AppSubcomponents
