/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.update.di;

import androidx.annotation.NonNull;

import com.kavsdkexample.update.presenter.UpdatePresenter;
import com.kavsdkexample.update.presenter.impl.UpdatePresenterImpl;
import com.kavsdkexample.core.app.di.PerFragment;

import dagger.Binds;
import dagger.Module;

@Module
abstract class FragmentsModule {
    @PerFragment
    @NonNull
    @Binds abstract UpdatePresenter provideUpdatePresenter(@NonNull UpdatePresenterImpl presenter);
}
