/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.root_checker.di;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.root_checker.presenter.RootCheckPresenter;
import com.kavsdkexample.antivirus.root_checker.presenter.impl.RootCheckPresenterImpl;
import com.kavsdkexample.core.app.di.PerFragment;

import dagger.Binds;
import dagger.Module;

@Module
abstract class FragmentsModule {
    @PerFragment
    @NonNull
    @Binds abstract RootCheckPresenter provideRootCheckPresenter(@NonNull RootCheckPresenterImpl presenter);
 }
