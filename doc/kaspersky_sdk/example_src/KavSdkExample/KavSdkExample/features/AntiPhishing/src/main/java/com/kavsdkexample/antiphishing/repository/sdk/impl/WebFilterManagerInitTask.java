/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antiphishing.repository.sdk.impl;

import androidx.annotation.NonNull;

import com.kavsdk.license.SdkLicenseViolationException;
import com.kavsdkexample.antiphishing.model.WebFilterInitObserver;
import com.kavsdkexample.core.app.utils.ThreadManager;

public final class WebFilterManagerInitTask implements Runnable {
    private final ThreadManager         mThreadManager;
    private final WebFilterManagerImpl  mWebFilterManager;
    private final WebFilterInitObserver mInitObserver;

    WebFilterManagerInitTask(@NonNull ThreadManager threadManager,
                             @NonNull WebFilterManagerImpl webFilterManager,
                             @NonNull WebFilterInitObserver initObserver) {
        mThreadManager       = threadManager;
        mWebFilterManager    = webFilterManager;
        mInitObserver        = initObserver;
    }

    @Override
    public void run() {
        try {
            mWebFilterManager.initWebFilter();
        } catch (SdkLicenseViolationException e) {
            mThreadManager.runOnUiThread(() -> mInitObserver.onWebFilterInitFailed(e));
            return;
        }
        mThreadManager.runOnUiThread(mInitObserver::onWebFilterInitSuccess);
    }
}
