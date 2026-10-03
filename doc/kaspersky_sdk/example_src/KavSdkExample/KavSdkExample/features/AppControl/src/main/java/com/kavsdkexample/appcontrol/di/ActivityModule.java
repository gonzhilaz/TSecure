/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.appcontrol.di;

import androidx.annotation.NonNull;

import com.kavsdkexample.appcontrol.presenter.AppControlPresenter;
import com.kavsdkexample.appcontrol.presenter.impl.AppControlPresenterImpl;
import com.kavsdkexample.core.app.di.PerActivity;

import dagger.Binds;
import dagger.Module;

@Module
abstract class ActivityModule {
    @PerActivity
    @Binds
    @NonNull
    abstract AppControlPresenter AppControlPresenter(@NonNull AppControlPresenterImpl presenter);

}


