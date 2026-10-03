/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.self_checker.di;

import com.kavsdkexample.antivirus.self_checker.view.impl.SelfCheckDialogFragment;
import com.kavsdkexample.core.app.di.PerFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
abstract class FragmentsBuilder {
    @ContributesAndroidInjector(modules = { FragmentsModule.class })
    @PerFragment
    abstract SelfCheckDialogFragment bindSelfCheckFragment();
}
