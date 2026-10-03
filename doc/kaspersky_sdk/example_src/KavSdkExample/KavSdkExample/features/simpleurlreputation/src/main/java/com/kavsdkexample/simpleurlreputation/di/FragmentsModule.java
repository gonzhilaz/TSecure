/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.simpleurlreputation.di;

import androidx.annotation.NonNull;

import com.kavsdkexample.simpleurlreputation.presenter.SimpleUrlReputationPresenter;
import com.kavsdkexample.simpleurlreputation.presenter.impl.SimpleUrlReputationPresenterImpl;
import com.kavsdkexample.core.app.di.PerFragment;

import dagger.Binds;
import dagger.Module;

@Module
abstract class FragmentsModule {
    @PerFragment
    @NonNull
    @Binds
    abstract SimpleUrlReputationPresenter providePresenter(@NonNull SimpleUrlReputationPresenterImpl presenter);
}
