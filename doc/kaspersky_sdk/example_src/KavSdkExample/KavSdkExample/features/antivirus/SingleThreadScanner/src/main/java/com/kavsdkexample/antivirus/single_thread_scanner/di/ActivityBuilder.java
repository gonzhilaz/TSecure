/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.single_thread_scanner.di;

import com.kavsdkexample.antivirus.base.view.impl.DetectedApplicationsActivity;
import com.kavsdkexample.core.app.di.PerActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
abstract class ActivityBuilder {
    @ContributesAndroidInjector(modules = {ActivityModule.class})
    @PerActivity
    abstract DetectedApplicationsActivity bindDetectedApplicationsActivity();
}
