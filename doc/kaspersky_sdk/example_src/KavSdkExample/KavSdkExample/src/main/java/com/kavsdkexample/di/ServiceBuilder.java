/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.di;

import com.kavsdkexample.model.service.SampleService;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
abstract class ServiceBuilder {
    @ContributesAndroidInjector
    abstract SampleService bindSampleService();
}
