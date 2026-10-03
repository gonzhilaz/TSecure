/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_connectivity.di;

import androidx.annotation.NonNull;

import com.kavsdkexample.core.app.di.PerActivity;
import com.kavsdkexample.secure_connectivity.presenter.SecureConnectionPresenter;
import com.kavsdkexample.secure_connectivity.presenter.impl.SecureConnectionPresenterImpl;

import dagger.Binds;
import dagger.Module;

@Module
abstract class ActivityModule {
    @PerActivity
    @NonNull
    @Binds
    abstract SecureConnectionPresenter bindSecureConnectionPresenter(@NonNull SecureConnectionPresenterImpl presenter);

}


