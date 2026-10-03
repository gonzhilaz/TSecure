/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.fingerprintmonitor.di;

import androidx.annotation.NonNull;

import com.kavsdkexample.core.app.di.PerActivity;
import com.kavsdkexample.fingerprintmonitor.presenter.FingerprintMonitorPresenter;
import com.kavsdkexample.fingerprintmonitor.presenter.impl.FingerprintMonitorPresenterImpl;

import dagger.Binds;
import dagger.Module;

@Module
abstract class ActivityModule {
    @PerActivity
    @Binds
    @NonNull
    abstract FingerprintMonitorPresenter provideFingerprintMonitorPresenter(@NonNull FingerprintMonitorPresenterImpl presenter);
}
