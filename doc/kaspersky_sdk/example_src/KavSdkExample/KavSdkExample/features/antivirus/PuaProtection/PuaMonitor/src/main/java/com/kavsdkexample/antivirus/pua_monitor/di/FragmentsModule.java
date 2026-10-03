/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.pua_monitor.di;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.pua_monitor.presenter.PuaMonitorPresenter;
import com.kavsdkexample.antivirus.pua_monitor.presenter.impl.PuaMonitorPresenterImpl;
import com.kavsdkexample.core.app.di.PerFragment;

import dagger.Binds;
import dagger.Module;

@Module
abstract class FragmentsModule {
    @PerFragment
    @NonNull
    @Binds
    abstract PuaMonitorPresenter providePuaMonitorPresenter(@NonNull PuaMonitorPresenterImpl presenter);
}