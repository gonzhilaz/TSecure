/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.model.app.sdk.impl;

import com.kavsdk.KavSdk;
import com.kavsdk.license.SdkLicense;
import com.kavsdkexample.model.app.sdk.Sdk;

public final class AvOrFullSdk implements Sdk {
    private static volatile AvOrFullSdk sInstance;

    public static AvOrFullSdk getInstance() {
        AvOrFullSdk instance = sInstance;
        if (instance == null) {
            synchronized (AvOrFullSdk.class) {
                instance = sInstance;
                if (instance == null) {
                    instance = sInstance = new AvOrFullSdk();
                }
            }
        }
        return instance;
    }

    private AvOrFullSdk() {

    }

    @Override
    public boolean isInitialized() {
        return KavSdk.isInitialized();
    }

    @Override
    public SdkLicense getLicense() {
        return KavSdk.getLicense();
    }

    @Override
    public String getHashOfHardwareId() {
        return KavSdk.getHashOfHardwareId();
    }

    @Override
    public String getInstallationId() {
        return KavSdk.getInstallationId();
    }

    @Override
    public void setProxyAuthCredentials(String login, String password) {
        KavSdk.setProxyAuthCredentials(login, password);
    }

    @Override
    public void cancelProxyAuth() {
        KavSdk.cancelProxyAuth();
    }
}
