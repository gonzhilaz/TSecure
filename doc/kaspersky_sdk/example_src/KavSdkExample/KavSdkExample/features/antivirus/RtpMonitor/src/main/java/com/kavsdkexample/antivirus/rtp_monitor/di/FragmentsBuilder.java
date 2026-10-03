/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.rtp_monitor.di;

import com.kavsdkexample.antivirus.rtp_monitor.view.impl.DirectoryManagerFragment;
import com.kavsdkexample.antivirus.rtp_monitor.view.impl.RemoveDialogFragment;
import com.kavsdkexample.antivirus.rtp_monitor.view.impl.RtpMonitorFragment;
import com.kavsdkexample.core.app.di.PerFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
abstract class FragmentsBuilder {
    @ContributesAndroidInjector(modules = { FragmentsModule.class })
    @PerFragment
    abstract RtpMonitorFragment bindRtpMonitorFragment();

    @ContributesAndroidInjector(modules = { FragmentsModule.class })
    @PerFragment
    abstract DirectoryManagerFragment bindDirectoryManagerFragment();

    @ContributesAndroidInjector(modules = { FragmentsModule.class })
    @PerFragment
    abstract RemoveDialogFragment bindRemoveDialogFragment();
}
