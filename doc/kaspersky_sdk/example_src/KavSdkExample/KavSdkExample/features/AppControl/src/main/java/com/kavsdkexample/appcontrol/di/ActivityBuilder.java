/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.appcontrol.di;

import com.kavsdkexample.appcontrol.view.impl.AppControlActivity;
import com.kavsdkexample.core.app.di.PerActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
abstract class ActivityBuilder {
    @ContributesAndroidInjector(modules = ActivityModule.class)
    @PerActivity
    abstract AppControlActivity bindMainActivity();
}
