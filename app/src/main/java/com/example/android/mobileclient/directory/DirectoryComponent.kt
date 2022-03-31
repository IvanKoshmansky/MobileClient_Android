package com.example.android.mobileclient.directory

import com.example.android.mobileclient.main.MainActivity
import com.example.android.mobileclient.paramdetail.ParamDetailFragment
import dagger.Subcomponent

@DirectoryScope
@Subcomponent
interface DirectoryComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): DirectoryComponent
    }
}
