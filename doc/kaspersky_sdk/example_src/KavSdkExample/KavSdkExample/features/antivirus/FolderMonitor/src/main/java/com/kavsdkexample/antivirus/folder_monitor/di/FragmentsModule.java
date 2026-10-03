/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.folder_monitor.di;


import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.folder_monitor.presenter.FolderMonitorPresenter;
import com.kavsdkexample.antivirus.folder_monitor.presenter.FoldersActionPresenter;
import com.kavsdkexample.antivirus.folder_monitor.presenter.impl.FolderMonitorPresenterImpl;
import com.kavsdkexample.antivirus.folder_monitor.presenter.impl.FoldersActionPresenterImpl;
import com.kavsdkexample.core.app.di.PerFragment;

import dagger.Binds;
import dagger.Module;

@Module
abstract class FragmentsModule {
    @PerFragment
    @NonNull
    @Binds
    abstract FolderMonitorPresenter provideRtpMonitorPresenter(@NonNull FolderMonitorPresenterImpl presenter);

    @PerFragment
    @NonNull
    @Binds
    abstract FoldersActionPresenter provideFoldersActionPresenter(@NonNull FoldersActionPresenterImpl presenter);
}
