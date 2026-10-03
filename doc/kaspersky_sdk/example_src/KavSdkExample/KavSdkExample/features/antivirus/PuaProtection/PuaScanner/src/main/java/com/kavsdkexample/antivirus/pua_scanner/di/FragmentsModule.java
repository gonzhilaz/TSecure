/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.pua_scanner.di;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.pua_scanner.presenter.PuaScanPresenter;
import com.kavsdkexample.antivirus.pua_scanner.presenter.impl.PuaScanPresenterImpl;
import com.kavsdkexample.core.app.di.PerFragment;

import dagger.Binds;
import dagger.Module;

@Module
abstract class FragmentsModule {
    @PerFragment
    @NonNull
    @Binds abstract PuaScanPresenter provideScanPresenter(@NonNull PuaScanPresenterImpl presenter);
}