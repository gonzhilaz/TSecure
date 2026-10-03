/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.rtp_monitor.di;


import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.rtp_monitor.presenter.DirectoryManagerPresenter;
import com.kavsdkexample.antivirus.rtp_monitor.presenter.RemoveDialogPresenter;
import com.kavsdkexample.antivirus.rtp_monitor.presenter.RtpMonitorPresenter;
import com.kavsdkexample.antivirus.rtp_monitor.presenter.impl.DirectoryManagerPresenterImpl;
import com.kavsdkexample.antivirus.rtp_monitor.presenter.impl.RemoveDialogPresenterImpl;
import com.kavsdkexample.antivirus.rtp_monitor.presenter.impl.RtpMonitorPresenterImpl;
import com.kavsdkexample.core.app.di.PerFragment;

import dagger.Binds;
import dagger.Module;

@Module
abstract class FragmentsModule {
    @PerFragment
    @NonNull
    @Binds
    abstract RtpMonitorPresenter provideRtpMonitorPresenter(@NonNull RtpMonitorPresenterImpl presenter);

    @PerFragment
    @NonNull
    @Binds
    abstract DirectoryManagerPresenter provideDirectoryManagerPresenter(@NonNull DirectoryManagerPresenterImpl presenter);

    @PerFragment
    @NonNull
    @Binds
    abstract RemoveDialogPresenter provideRemoveDialogPresenter(@NonNull RemoveDialogPresenterImpl presenter);
}
