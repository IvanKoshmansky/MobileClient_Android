package com.example.android.mobileclient.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.android.mobileclient.MobileClientApplication
import com.example.android.mobileclient.R
import com.example.android.mobileclient.data.DataComponent
import com.example.android.mobileclient.databinding.ActivityMainBinding
import com.example.android.mobileclient.device.DeviceComponent
import com.example.android.mobileclient.directory.DirectoryComponent
import com.example.android.mobileclient.overview.OverviewComponent
import com.example.android.mobileclient.paramdetail.ParamDetailComponent

class MainActivity : AppCompatActivity() {

    lateinit var mainComponent: MainComponent
    lateinit var overviewComponent: OverviewComponent
    lateinit var paramDetailComponent: ParamDetailComponent
    lateinit var dataComponent: DataComponent
    lateinit var deviceComponent: DeviceComponent
    lateinit var directoryComponent: DirectoryComponent

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {

        mainComponent = (application as MobileClientApplication).appComponent.mainComponent().create()
        mainComponent.inject(this)
        overviewComponent = (application as MobileClientApplication).appComponent.overviewComponent().create()
        paramDetailComponent = (application as MobileClientApplication).appComponent.paramDetailComponent().create()
        dataComponent = (application as MobileClientApplication).appComponent.dataComponent().create()
        deviceComponent = (application as MobileClientApplication).appComponent.deviceManager().deviceComponent
        directoryComponent = (application as MobileClientApplication).appComponent.directoryManager().directoryComponent

        // после инжекций
        super.onCreate(savedInstanceState)

        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)

        drawerLayout = binding.drawerLayout

        val navController = this.findNavController(R.id.navHostFragment)

        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)

        NavigationUI.setupWithNavController(binding.navView, navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.navHostFragment)
        return NavigationUI.navigateUp(navController, drawerLayout)
    }
}
