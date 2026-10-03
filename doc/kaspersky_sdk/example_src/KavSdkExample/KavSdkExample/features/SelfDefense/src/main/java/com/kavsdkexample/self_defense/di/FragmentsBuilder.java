/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.self_defense.di;

import com.kavsdkexample.core.app.di.PerFragment;
import com.kavsdkexample.self_defense.view.impl.CheckSignatureFragment;
import com.kavsdkexample.self_defense.view.impl.SelfDefenseFeatureFragment;
import com.kavsdkexample.self_defense.view.impl.SelfDefenseMainFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
abstract class FragmentsBuilder {
    @ContributesAndroidInjector(modules = { FragmentsModule.class })
    @PerFragment
    abstract SelfDefenseFeatureFragment bindSelfDefenseFeatureFragment();

    @ContributesAndroidInjector(modules = { FragmentsModule.class })
    @PerFragment
    abstract SelfDefenseMainFragment bindSelfDefenseMainFragment();

    @ContributesAndroidInjector(modules = { FragmentsModule.class })
    @PerFragment
    abstract CheckSignatureFragment bindCheckSignatureFragment();
}
