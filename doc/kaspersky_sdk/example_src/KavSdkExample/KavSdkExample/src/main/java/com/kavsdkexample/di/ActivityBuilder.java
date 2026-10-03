/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.di;

import com.kavsdkexample.core.app.di.PerActivity;
import com.kavsdkexample.core.app.di.PerFragment;
import com.kavsdkexample.view.impl.MainActivity;
import com.kavsdkexample.view.permissions.impl.InsufficientPermissionsFragment;
import com.kavsdkexample.view.sdk.anti_phishing.impl.AntiPhishingFragment;
import com.kavsdkexample.view.sdk.anti_virus.impl.AntiVirusContentsFragment;
import com.kavsdkexample.view.sdk.anti_virus.impl.AntiVirusFragment;
import com.kavsdkexample.view.sdk.data_protection.impl.DataProtectionFragment;
import com.kavsdkexample.view.sdk.device_reputation.impl.DeviceReputationFragment;
import com.kavsdkexample.view.sdk.impl.ProxyAuthActivity;
import com.kavsdkexample.view.sdk.license.impl.LicenseInfoFragment;
import com.kavsdkexample.view.sdk.other_features.impl.OtherFeaturesFragment;
import com.kavsdkexample.view.sdk.privacy.impl.PrivacyFragment;
import com.kavsdkexample.view.sdk.secure_connectivity.impl.SecureConnectivityFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
abstract class ActivityBuilder {
    @ContributesAndroidInjector(modules = {ActivityModule.class})
    @PerActivity
    abstract MainActivity bindMainActivity();

    @ContributesAndroidInjector(modules = {ActivityModule.class})
    @PerActivity
    abstract ProxyAuthActivity bindProxyAuthActivity();

    @ContributesAndroidInjector(modules = {FragmentModule.class})
    @PerFragment
    abstract InsufficientPermissionsFragment bindPermissionFragment();

    @ContributesAndroidInjector(modules = {FragmentModule.class})
    @PerFragment
    abstract LicenseInfoFragment bindLicenseInfoFragment();

    @ContributesAndroidInjector(modules = {FragmentModule.class})
    @PerFragment
    abstract AntiPhishingFragment bindAntiPhishingFragment();

    @ContributesAndroidInjector(modules = {FragmentModule.class})
    @PerFragment
    abstract AntiVirusFragment bindAntiVirusFragment();

    @ContributesAndroidInjector(modules = {FragmentModule.class})
    @PerFragment
    abstract AntiVirusContentsFragment bindAntiVirusContentsFragment();

    @ContributesAndroidInjector(modules = {FragmentModule.class})
    @PerFragment
    abstract DeviceReputationFragment bindDeviceReputationFragment();

    @ContributesAndroidInjector(modules = {FragmentModule.class})
    @PerFragment
    abstract DataProtectionFragment bindDataProtectionFragment();

    @ContributesAndroidInjector(modules = {FragmentModule.class})
    @PerFragment
    abstract SecureConnectivityFragment bindSecureConnectivityFragment();

    @ContributesAndroidInjector(modules = {FragmentModule.class})
    @PerFragment
    abstract OtherFeaturesFragment bindOtherFeaturesFragment();

    @ContributesAndroidInjector(modules = {FragmentModule.class})
    @PerFragment
    abstract PrivacyFragment bindPrivacyFragment();
}
