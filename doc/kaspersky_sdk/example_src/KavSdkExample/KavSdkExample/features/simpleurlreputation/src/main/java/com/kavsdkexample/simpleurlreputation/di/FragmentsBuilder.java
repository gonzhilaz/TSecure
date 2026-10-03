/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.simpleurlreputation.di;

import com.kavsdkexample.simpleurlreputation.view.impl.SimpleUrlReputationFragment;
import com.kavsdkexample.core.app.di.PerFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
abstract class FragmentsBuilder {
    @ContributesAndroidInjector(modules = { FragmentsModule.class })
    @PerFragment
    abstract SimpleUrlReputationFragment bindSimpleUrlReputationFragment();
}
