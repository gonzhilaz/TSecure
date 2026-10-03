/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.eula.di;

import com.kavsdkexample.core.app.di.PerFragment;
import com.kavsdkexample.eula.view.impl.EulaFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
abstract class FragmentsBuilder {
    @ContributesAndroidInjector(modules = { FragmentsModule.class })
    @PerFragment
    abstract EulaFragment bindEulaFragment();
}
