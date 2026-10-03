/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.single_thread_scanner.di;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.base.presenter.DetectedApplicationsPresenter;
import com.kavsdkexample.antivirus.base.presenter.ViewSwitcherPresenter;
import com.kavsdkexample.antivirus.base.presenter.impl.DetectedApplicationsPresenterImpl;
import com.kavsdkexample.antivirus.base.presenter.impl.ViewSwitcherPresenterImpl;
import com.kavsdkexample.antivirus.single_thread_scanner.presenter.SingleThreadScanResultsPresenter;
import com.kavsdkexample.antivirus.single_thread_scanner.presenter.SingleThreadScannerPresenter;
import com.kavsdkexample.antivirus.single_thread_scanner.presenter.impl.SingleThreadScanResultsPresenterImpl;
import com.kavsdkexample.antivirus.single_thread_scanner.presenter.impl.SingleThreadScannerPresenterImpl;
import com.kavsdkexample.core.app.di.PerFragment;
import com.kavsdkexample.core.app.presenter.EmptyPresenter;
import com.kavsdkexample.core.app.presenter.impl.EmptyPresenterImpl;

import dagger.Binds;
import dagger.Module;

@Module
abstract class FragmentsModule {
    @PerFragment
    @NonNull
    @Binds abstract ViewSwitcherPresenter provideViewSwitcherPresenter(@NonNull ViewSwitcherPresenterImpl presenter);
    @PerFragment
    @NonNull
    @Binds abstract EmptyPresenter provideEmptyPresenter(@NonNull EmptyPresenterImpl presenter);
    @PerFragment
    @NonNull
    @Binds abstract DetectedApplicationsPresenter provideDetectedAppsPresenter(@NonNull DetectedApplicationsPresenterImpl presenter);
    @PerFragment
    @NonNull
    @Binds abstract SingleThreadScannerPresenter provideSingleThreadScannerPresenter(@NonNull SingleThreadScannerPresenterImpl presenter);
    @PerFragment
    @NonNull
    @Binds abstract SingleThreadScanResultsPresenter provideScanResultsPresenter(@NonNull SingleThreadScanResultsPresenterImpl presenter);
 }
