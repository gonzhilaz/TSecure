/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.model.app.sdk.impl;

import androidx.annotation.NonNull;

import com.kaspersky.whocalls.externalapi.WhoCallsSdkFactory;
import com.kavsdk.license.SdkLicense;
import com.kavsdkexample.model.app.sdk.Sdk;

public final class WcSdk implements Sdk {
    private final boolean mInitialized;

    public WcSdk(final boolean initialized) {
        mInitialized = initialized;
    }

    @Override
    public boolean isInitialized() {
        return mInitialized;
    }

    @NonNull
    @Override
    public SdkLicense getLicense() {
        return WhoCallsSdkFactory.getLicense();
    }

    @Override
    public String getHashOfHardwareId() {
        return WhoCallsSdkFactory.getHashOfHardwareId();
    }

    @Override
    public String getInstallationId() {
        return WhoCallsSdkFactory.getInstallationId();
    }

    @Override
    public void setProxyAuthCredentials(String login, String password) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cancelProxyAuth() {
        throw new UnsupportedOperationException();
    }
}
