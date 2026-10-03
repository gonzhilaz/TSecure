/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.root_checker.repository.sdk.impl;

import androidx.annotation.NonNull;

import com.kavsdk.antivirus.AntivirusInstance;
import com.kavsdk.license.SdkLicenseViolationException;
import com.kavsdk.rootdetector.RootDetector;
import com.kavsdkexample.antivirus.root_checker.model.RootCheckResultsObserver;
import com.kavsdkexample.core.app.utils.ThreadManager;

public final class RootCheckTask implements Runnable {
    private ThreadManager            mThreadManager;
    private RootCheckResultsObserver mObserver;

    RootCheckTask(@NonNull ThreadManager threadManager,
                  @NonNull RootCheckResultsObserver observer) {
        mThreadManager = threadManager;
        mObserver      = observer;
    }

    @Override
    public void run() {
        if (!AntivirusInstance.getInstance().getVirusDbInfo().mAvailable) {
            mThreadManager.runOnUiThread(mObserver::onBasesUnavailable);
            return;
        }

        try {
            boolean isRooted = RootDetector.getInstance().checkRoot();
            mThreadManager.runOnUiThread(() -> mObserver.onSuccess(isRooted));
        } catch (SdkLicenseViolationException e) {
            mThreadManager.runOnUiThread(() -> mObserver.onFailed(e));
        }
    }
}
