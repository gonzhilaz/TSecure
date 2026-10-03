/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.di;

import com.kavsdkexample.secure_storage.view.impl.TestDatabaseActivity;
import com.kavsdkexample.secure_storage.view.impl.TestSecureFileActivity;
import com.kavsdkexample.core.app.di.PerActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
abstract class ActivityBuilder {
    @ContributesAndroidInjector(modules = {ActivityModule.class, FragmentsBuilder.class})
    @PerActivity
    abstract TestDatabaseActivity bindDbActivity();

    @ContributesAndroidInjector(modules = {ActivityModule.class, FragmentsBuilder.class})
    @PerActivity
    abstract TestSecureFileActivity bindFileActivity();
}
