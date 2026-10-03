/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antiphishing.di;

import com.kavsdkexample.antiphishing.view.impl.WebFilterActivity;
import com.kavsdkexample.core.app.di.PerActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
abstract class ActivityBuilder {
    @ContributesAndroidInjector(modules = ActivityModule.class)
    @PerActivity
    abstract WebFilterActivity bindMainActivity();
}
