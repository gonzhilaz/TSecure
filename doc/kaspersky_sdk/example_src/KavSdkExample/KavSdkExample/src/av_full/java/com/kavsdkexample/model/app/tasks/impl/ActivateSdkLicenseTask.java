/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.model.app.tasks.impl;

import androidx.annotation.NonNull;

import com.kavsdk.KavSdk;
import com.kavsdk.license.SdkLicense;
import com.kavsdk.license.SdkLicenseException;
import com.kavsdkexample.BuildConfig;
import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.model.app.tasks.ActivateLicenseResultsObserver;

public final class ActivateSdkLicenseTask implements Runnable {
    private final ThreadManager                  mThreadManager;
    private final ActivateLicenseResultsObserver mObserver;
    private final String                         mActivationCode;

    ActivateSdkLicenseTask(@NonNull ThreadManager                  threadManager,
                           @NonNull ActivateLicenseResultsObserver observer,
                           @NonNull String                         activationCode) {
        mThreadManager  = threadManager;
        mObserver       = observer;
        mActivationCode = activationCode;
    }

    @Override
    public void run() {
        if (BuildConfig.DEBUG) {
            mThreadManager.checkWorkerThread();
        }
        try {
            SdkLicense license = KavSdk.getLicense();
            if (license.isClientUserIDRequired()) {
                license.sendClientUserID(mActivationCode);
            } else {
                license.activate(mActivationCode);
            }
        } catch (SdkLicenseException e) {
            mThreadManager.runOnUiThread(() -> mObserver.onFailed(e));
            return;
        }
        mThreadManager.runOnUiThread(() -> mObserver.onSuccess(KavSdk.getLicense().getLicenseKeyExpireDate()));
    }
}
