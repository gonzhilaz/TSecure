/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.fingerprintmonitor.di;

import com.kavsdkexample.core.app.di.PerActivity;
import com.kavsdkexample.fingerprintmonitor.view.impl.FingerprintMonitorActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
abstract class ActivityBuilder {
    @ContributesAndroidInjector(modules = ActivityModule.class)
    @PerActivity
    abstract FingerprintMonitorActivity bindMainActivity();
}
