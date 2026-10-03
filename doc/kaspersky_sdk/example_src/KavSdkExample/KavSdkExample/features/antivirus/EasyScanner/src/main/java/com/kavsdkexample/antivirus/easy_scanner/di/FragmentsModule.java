/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.easy_scanner.di;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.base.presenter.DetectedApplicationsPresenter;
import com.kavsdkexample.antivirus.base.presenter.ViewSwitcherPresenter;
import com.kavsdkexample.antivirus.base.presenter.impl.DetectedApplicationsPresenterImpl;
import com.kavsdkexample.antivirus.base.presenter.impl.ViewSwitcherPresenterImpl;
import com.kavsdkexample.antivirus.easy_scanner.presenter.EasyScanResultsPresenter;
import com.kavsdkexample.antivirus.easy_scanner.presenter.EasyScannerPresenter;
import com.kavsdkexample.antivirus.easy_scanner.presenter.impl.EasyScanResultsPresenterImpl;
import com.kavsdkexample.antivirus.easy_scanner.presenter.impl.EasyScannerPresenterImpl;
import com.kavsdkexample.core.app.di.PerFragment;
import com.kavsdkexample.core.app.presenter.EmptyPresenter;
import com.kavsdkexample.core.app.presenter.impl.EmptyPresenterImpl;

import dagger.Binds;
import dagger.Module;

@Module
abstract class FragmentsModule {
    @PerFragment
    @NonNull
    @Binds abstract EasyScannerPresenter provideScanPresenter(@NonNull EasyScannerPresenterImpl presenter);
    @PerFragment
    @NonNull
    @Binds abstract EasyScanResultsPresenter provideScanResultsPresenter(@NonNull EasyScanResultsPresenterImpl presenter);
    @PerFragment
    @NonNull
    @Binds abstract ViewSwitcherPresenter provideViewSwitcherPresenter(@NonNull ViewSwitcherPresenterImpl presenter);
    @PerFragment
    @NonNull
    @Binds abstract EmptyPresenter provideEmptyPresenter(@NonNull EmptyPresenterImpl presenter);
    @PerFragment
    @NonNull
    @Binds abstract DetectedApplicationsPresenter provideDetectedAppsPresenter(@NonNull DetectedApplicationsPresenterImpl presenter);
 }
