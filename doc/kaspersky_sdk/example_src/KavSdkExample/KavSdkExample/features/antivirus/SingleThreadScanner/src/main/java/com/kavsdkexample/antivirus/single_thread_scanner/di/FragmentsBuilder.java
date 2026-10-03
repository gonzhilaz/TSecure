/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.single_thread_scanner.di;

import com.kavsdkexample.antivirus.base.view.impl.LastScanThreatsFragment;
import com.kavsdkexample.antivirus.single_thread_scanner.view.impl.SingleThreadScannerFragment;
import com.kavsdkexample.antivirus.single_thread_scanner.view.impl.SingleThreadScannerMainFragment;
import com.kavsdkexample.antivirus.single_thread_scanner.view.impl.SingleThreadScannerResultsFragment;
import com.kavsdkexample.core.app.di.PerFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
abstract class FragmentsBuilder {
    @ContributesAndroidInjector(modules = { FragmentsModule.class })
    @PerFragment
    abstract LastScanThreatsFragment bindLastScanThreatsFragment();

    @ContributesAndroidInjector(modules = { FragmentsModule.class })
    @PerFragment
    abstract SingleThreadScannerMainFragment bindSingleThreadScannerMainFragment();

    @ContributesAndroidInjector(modules = { FragmentsModule.class })
    @PerFragment
    abstract SingleThreadScannerResultsFragment bindSingleThreadScannerResultsFragment();

    @ContributesAndroidInjector(modules = { FragmentsModule.class })
    @PerFragment
    abstract SingleThreadScannerFragment bindSingleThreadScannerFragment();
}
