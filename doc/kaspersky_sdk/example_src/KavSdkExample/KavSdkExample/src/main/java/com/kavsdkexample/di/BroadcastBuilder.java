/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.di;

import com.kavsdkexample.model.broadcasts.Autoloader;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
abstract class BroadcastBuilder {
    @ContributesAndroidInjector
    abstract Autoloader bindAutoloaderBroadcast();
}
