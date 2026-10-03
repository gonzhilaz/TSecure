/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_connectivity.di;

import com.kavsdkexample.core.app.di.PerActivity;
import com.kavsdkexample.secure_connectivity.nonmvp.DnsCheckerActivity;
import com.kavsdkexample.secure_connectivity.nonmvp.SecureConnectionActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
abstract class ActivityBuilder {
    @ContributesAndroidInjector(modules = ActivityModule.class)
    @PerActivity
    abstract SecureConnectionActivity bindSecureConnectionActivity();

    @ContributesAndroidInjector(modules = ActivityModule.class)
    @PerActivity
    abstract DnsCheckerActivity bindDnsChecherActivity();
}
