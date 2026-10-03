/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.single_thread_scanner.di;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.base.model.AntivirusModel;
import com.kavsdkexample.antivirus.base.presenter.DetectedApplicationsPresenter;
import com.kavsdkexample.antivirus.base.presenter.impl.DetectedApplicationsPresenterImpl;
import com.kavsdkexample.core.app.di.PerActivity;

import dagger.Module;
import dagger.Provides;

@Module
class ActivityModule {

    @PerActivity
    @Provides
    @NonNull
    DetectedApplicationsPresenter provideApplicationsPresenter(@NonNull AntivirusModel model) {
        return new DetectedApplicationsPresenterImpl(model);
    }

}
