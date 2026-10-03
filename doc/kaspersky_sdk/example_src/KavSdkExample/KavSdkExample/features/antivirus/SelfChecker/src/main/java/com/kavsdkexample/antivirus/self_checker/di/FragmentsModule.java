/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.self_checker.di;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.self_checker.presenter.SelfCheckPresenter;
import com.kavsdkexample.antivirus.self_checker.presenter.impl.SelfCheckPresenterImpl;
import com.kavsdkexample.core.app.di.PerFragment;

import dagger.Binds;
import dagger.Module;

@Module
abstract class FragmentsModule {
    @PerFragment
    @NonNull
    @Binds abstract SelfCheckPresenter provideSelfCheckPresenter(@NonNull SelfCheckPresenterImpl presenter);
 }
