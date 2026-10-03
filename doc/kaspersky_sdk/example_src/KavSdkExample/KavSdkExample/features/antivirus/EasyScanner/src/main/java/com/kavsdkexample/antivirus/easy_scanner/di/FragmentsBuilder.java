/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.easy_scanner.di;

import com.kavsdkexample.antivirus.base.view.impl.LastScanThreatsFragment;
import com.kavsdkexample.antivirus.easy_scanner.view.impl.EasyScanResultsFragment;
import com.kavsdkexample.antivirus.easy_scanner.view.impl.EasyScannerFragment;
import com.kavsdkexample.antivirus.easy_scanner.view.impl.EasyScannerMainFragment;
import com.kavsdkexample.core.app.di.PerFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
abstract class FragmentsBuilder {
    @ContributesAndroidInjector(modules = { FragmentsModule.class })
    @PerFragment
    abstract EasyScannerFragment bindEasyScannerFragment();

    @ContributesAndroidInjector(modules = { FragmentsModule.class })
    @PerFragment
    abstract EasyScanResultsFragment bindEasyScanResultsFragment();

    @ContributesAndroidInjector(modules = { FragmentsModule.class })
    @PerFragment
    abstract EasyScannerMainFragment bindEasyScannerMainFragment();

    @ContributesAndroidInjector(modules = { FragmentsModule.class })
    @PerFragment
    abstract LastScanThreatsFragment bindLastScanThreatsFragment();
}
