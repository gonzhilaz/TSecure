/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.pua_scanner.di;

import com.kavsdkexample.antivirus.pua_scanner.view.impl.PuaScannerFragment;
import com.kavsdkexample.core.app.di.PerFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
abstract class FragmentsBuilder {
    @ContributesAndroidInjector(modules = { FragmentsModule.class })
    @PerFragment
    abstract PuaScannerFragment bindPuaScannerFragment();
}